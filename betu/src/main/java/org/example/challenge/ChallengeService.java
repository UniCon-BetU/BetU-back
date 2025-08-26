package org.example.challenge;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.challenge.dto.*;
import org.example.challenge.entity.*;
import org.example.challenge.repository.ChallengeLikeRepository;
import org.example.challenge.repository.ChallengeRepository;
import org.example.challenge.repository.UserChallengeRepository;
import org.example.crew.entity.Crew;
import org.example.crew.entity.UserCrew;
import org.example.crew.entity.UserCrewRole;
import org.example.crew.repository.CrewRepository;
import org.example.crew.repository.UserCrewRepository;
import org.example.verification_image.VerificationImageRepository;
import org.example.user.User;
import org.example.user.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final VerificationImageRepository verificationImageRepository;
    private final CrewRepository crewRepository;

    private final UserRepository userRepository;
    private final UserCrewRepository userCrewRepository;
    private final ChallengeLikeRepository challengeLikeRepository;

    // 챌린지 생성
    public ChallengeDetailResponse create(Long userId, ChallengeCreateRequest dto) {
        // 1) 작성자(creator) = 토큰 사용자
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Crew crew = null;
        // GROUP 스코프일 때만 그룹/멤버십/소유자 검증
        if (dto.getChallengeScope() == ChallengeScope.CREW) {
            if (dto.getCrewId() == null) {
                throw new IllegalArgumentException("GROUP 스코프의 챌린지는 crewId가 필요합니다.");
            }

            crew = crewRepository.findById(dto.getCrewId())
                    .orElseThrow(() -> new RuntimeException("그룹을 찾을 수 없습니다."));

            // 멤버십 체크
            UserCrew userCrew = userCrewRepository
                    // 연관관계 매핑이라면 ↓ 이렇게 써야 함: findByUser_UserIdAndGroup_GroupId
                    .findByUser_UserIdAndCrew_CrewId(userId, dto.getCrewId())
                    .orElseThrow(() -> new RuntimeException("해당 그룹에 속해있지 않습니다."));

            // 소유자 체크
            if (userCrew.getUserCrewRole() != UserCrewRole.OWNER) {
                throw new AccessDeniedException("그룹 소유자만 CREW 스코프 챌린지를 생성할 수 있습니다.");
            }
        }

        // PERSONAL이면 group == null 유지
        Challenge challenge = dto.toEntity(crew, creator);
        Challenge saved = challengeRepository.save(challenge);

        userChallengeRepository.save(new UserChallenge(creator, saved, UserChallengeRole.CREATOR));

        return toDetailResponse(saved, false, 0);
    }

    // 챌린지 좋아요
    public ChallengeLikeResponse challengeLike(Long userId, Long challengeId) {
        Challenge ch = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("챌린지를 찾을 수 없습니다."));

        boolean exists = challengeLikeRepository.existsByUserIdAndChallengeId(userId, challengeId);
        if (exists) {
            challengeLikeRepository.deleteByUserIdAndChallengeId(userId, challengeId); // 해제
            ch.decreaseLikeCount();
            return new ChallengeLikeResponse(false);
        } else {
            challengeLikeRepository.save(new ChallengeLike(userId, challengeId)); // 저장
            ch.increaseLikeCount();
            return new ChallengeLikeResponse(true);
        }
    }

    // 챌린지 삭제
    @Transactional
    public void deleteChallenge(Long userId, Long challengeId) {
        // 생성자인지 확인
        boolean isCreator = userChallengeRepository
                .existsByUser_UserIdAndChallenge_ChallengeIdAndUserChallengeRole(userId, challengeId, UserChallengeRole.CREATOR);

        if (!isCreator) {
            throw new AccessDeniedException("챌린지 생성자만 삭제할 수 있습니다.");
        }

        // 존재 확인
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("챌린지를 찾을 수 없습니다."));

        verificationImageRepository.deleteAllByUserChallenge_Challenge_ChallengeId(challengeId);
        userChallengeRepository.deleteAllByChallenge_ChallengeId(challengeId);
        challengeLikeRepository.deleteAllByChallengeId(challengeId);
        challengeRepository.deleteById(challengeId);
    }

    // 하나의 챌린지 조회
    public ChallengeDetailResponse getChallengeWithDetails(Long userId, Long challengeId) {
        Challenge ch = challengeRepository.findWithCrewByChallengeId(challengeId)
                .orElseThrow(() -> new EntityNotFoundException("해당 챌린지를 찾을 수 없습니다."));
        Optional<UserChallenge> userChallengeOpt = userChallengeRepository
                .findByUser_UserIdAndChallenge_ChallengeId(userId, challengeId);

        if (userChallengeOpt.isPresent()) {
            UserChallenge uc = userChallengeOpt.get();
            return toDetailResponse(ch, true, uc.getProgressPercent());
        } else {
            return toDetailResponse(ch, false, 0);
        }
    }

    // 챌린지 참가
    @Transactional
    public ChallengeDetailResponse joinChallenge(Long userId, Long challengeId, BetAmountRequest betAmountRequest) {
        // 챌린지 확인
        Challenge challenge = challengeRepository.findWithCrewByChallengeId(challengeId)
                .orElseThrow(() -> new EntityNotFoundException("챌린지를 찾을 수 없습니다."));

        // 기존 참가 기록 있으면 재사용, 없으면 생성
        UserChallenge uc = userChallengeRepository
                .findByUser_UserIdAndChallenge_ChallengeId(userId, challengeId)
                .orElseGet(() -> {
                    // 새로 만들 때만 유저 조회 필요 (지연 참조로 과쿼리 방지)
                    User userRef = userRepository.getReferenceById(userId);
                    return new UserChallenge(userRef, challenge, UserChallengeRole.PARTICIPANT);
                });

        // 이미 진행 중이면 거부
        if (uc.getUserChallengeStatus() == UserChallengeStatus.IN_PROGRESS) {
            throw new IllegalStateException("이미 해당 챌린지에 참가 중입니다.");
        }

        // 포인트 차감
        User user = (uc.getUser() != null) ? uc.getUser() : userRepository.getReferenceById(userId);
        if (user.getPoint() < betAmountRequest.getBetAmount()) {
            throw new IllegalStateException("포인트가 부족합니다.");
        }
        user.subtractPoint(betAmountRequest.getBetAmount());  // User 엔티티에 subtractPoint(long) 필요

        // 4) 베팅 금액 기록 + 상태 전환
        UserChallengeStatus prev = uc.getUserChallengeStatus();
        uc.makeBetAmount(betAmountRequest.getBetAmount());                             // ✅ 베팅 포인트 기록
        uc.changeStatus(UserChallengeStatus.IN_PROGRESS);       // ✅ 참가 시작
        userChallengeRepository.save(uc);

        // 5) 참가자 수 증가 규칙
        if (prev != UserChallengeStatus.IN_PROGRESS) {
            challenge.increaseParticipantCount();
            challengeRepository.save(challenge);
        }

        return toDetailResponse(challenge, true, 0);
    }

    /** 성공 정산: 스테이크 전액 환급 + 랜덤 보너스, 상태 COMPLETED */
    @Transactional
    public SettleSuccessResponse settleSuccess(Long userId, Long challengeId) {
        UserChallenge uc = userChallengeRepository
                .findByUser_UserIdAndChallenge_ChallengeId(userId, challengeId)
                .orElseThrow(() -> new EntityNotFoundException("참여 이력 없음"));

        if (uc.getUserChallengeStatus() == UserChallengeStatus.COMPLETED) {
            throw new IllegalStateException("이미 완료된 챌린지입니다.");
        }
        if (uc.getBetAmount() == null || uc.getBetAmount() <= 0) {
            throw new IllegalStateException("베팅 금액이 없습니다.");
        }

        long refund = uc.getBetAmount();

        // 1) 스테이크 환급
        uc.getUser().addPoint(refund);

        // 2) 랜덤 보상 (0~100%)
        int percent = (int) Math.round(Math.random() * 100.0);
        long bonus = Math.round(refund * (percent / 100.0));
        if (bonus > 0) uc.getUser().addPoint(bonus);

        // 3) 상태 업데이트
        uc.changeStatus(UserChallengeStatus.COMPLETED);

        return new SettleSuccessResponse(refund, bonus);
    }

    /** 실패/취소: 스테이크 환급(정책상 전액/일부), 상태 FAILED */
    @Transactional
    public void cancelBet(Long userId, Long challengeId) {
        UserChallenge uc = userChallengeRepository
                .findByUser_UserIdAndChallenge_ChallengeId(userId, challengeId)
                .orElseThrow(() -> new EntityNotFoundException("참여 이력 없음"));

        if (uc.getUserChallengeStatus() == UserChallengeStatus.COMPLETED
                || uc.getUserChallengeStatus() == UserChallengeStatus.FAILED) {
            return; // 이미 종료 상태
        }
        long stake = uc.getBetAmount() == null ? 0L : uc.getBetAmount();
        if (stake <= 0) return;

        uc.changeStatus(UserChallengeStatus.FAILED);
    }

    // 인기 챌린지 3개 조회
    public List<ChallengeResponse> getPopularChallenges() {
        List<Challenge> top3 = challengeRepository.findTop3ByOrderByChallengeParticipantCntDesc();

        return top3.stream()
                .map(this::toResponse)
                .toList();
    }

    // 좋아요한 챌린지 조회
    public List<ChallengeResponse> getLikedChallenges(Long userId) {
        List<ChallengeLike> likes = challengeLikeRepository.findByUserId(userId);
        if (likes.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> challengeIds = likes.stream()
                .map(ChallengeLike::getChallengeId)
                .collect(Collectors.toList());

        List<Challenge> challenges = challengeRepository.findAllWithCrewByIdIn(challengeIds);

        return challenges.stream()
                .map(this::toResponse)
                .toList();
    }

    // 내 챌린지 조회
    public List<ChallengeResponse> getMyChallenges(Long userId) {
        List<UserChallenge> userChallenges = userChallengeRepository.findAllByUser_UserId(userId);
        if (userChallenges.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> challengeIds = userChallenges.stream()
                .map(uc -> uc.getChallenge().getChallengeId())
                .collect(Collectors.toList());

        List<Challenge> challenges = challengeRepository.findAllWithCrewByIdIn(challengeIds);

        return challenges.stream()
                .map(this::toResponse)
                .toList();
    }

    // 태그로 챌린지 조회 (하나라도 포함: OR)
    public List<ChallengeResponse> getChallengesByTags(Set<ChallengeTag> tags) {
        if (tags == null || tags.isEmpty()) return Collections.emptyList();
        List<Challenge> list = challengeRepository.findByAnyTagsWithCrew(tags);
        return list.stream().map(this::toResponse).toList();
    }

    // 챌린지 검색 (이름/설명 키워드)
    public List<ChallengeResponse> searchChallenges(String keyword) {
        if (keyword == null || keyword.isBlank()) return Collections.emptyList();
        List<Challenge> list = challengeRepository.searchWithCrew(keyword.trim());
        return list.stream().map(this::toResponse).toList();
    }

    // 전체 챌린지 조회
    public List<ChallengeResponse> getAllChallenges() {
        List<Challenge> list = challengeRepository.findAllWithCrew();
        return list.stream().map(this::toResponse).toList();
    }

    private ChallengeResponse toResponse(Challenge c) {
        return new ChallengeResponse(
                c.getChallengeId(),
                c.getChallengeScope(),
                c.getCrew(),
                c.getChallengeType(),
                c.getChallengeName(),
                c.getChallengeDescription(),
                c.getChallengeStartDate(),
                c.getChallengeEndDate(),
                c.getChallengeBetAmount(),
                c.getChallengeLikeCnt(),
                c.getChallengeParticipantCnt()
        );
    }

    private ChallengeDetailResponse toDetailResponse(Challenge c, boolean isParticipating, int progress) {
        return new ChallengeDetailResponse(
                c.getChallengeId(),
                c.getChallengeScope(),
                c.getCrew(),
                c.getTags(),
                c.getChallengeType(),
                c.getChallengeName(),
                c.getChallengeDescription(),
                c.getChallengeStartDate(),
                c.getChallengeEndDate(),
                c.getChallengeBetAmount(),
                c.getChallengeLikeCnt(),
                c.getChallengeParticipantCnt(),
                isParticipating,
                progress
        );
    }
}