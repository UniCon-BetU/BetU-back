package org.example.verification_image;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.challenge.entity.Challenge;
import org.example.challenge.entity.ChallengeScope;
import org.example.challenge.entity.UserChallenge;
import org.example.challenge.entity.UserChallengeStatus;
import org.example.challenge.repository.ChallengeRepository;
import org.example.challenge.repository.UserChallengeRepository;
import org.example.crew.entity.UserCrewRole;
import org.example.crew.repository.UserCrewRepository;
import org.example.general.S3Uploader;
import org.example.user.User;
import org.example.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class VerificationImageService {
    private final VerificationImageRepository verificationImageRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final S3Uploader s3Uploader; // S3 업로드 유틸 (이미 구현했다고 가정)
    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;
    private final UserCrewRepository userCrewRepository;

    // 인증 이미지 업로드
    public Long uploadVerification(Long userId, Long challengeId, MultipartFile image) throws IOException {
        UserChallenge userChallenge = userChallengeRepository
                .findByUser_UserIdAndChallenge_ChallengeId(userId, challengeId)
                .orElseThrow(() -> new RuntimeException("챌린지에 참가하지 않았습니다."));

        // S3 업로드
        String prefix = String.format("challenge/%d/user/%d", challengeId, userId);
        String imageUrl = s3Uploader.uploadImageUnderPrefix(image, prefix);

        VerificationImage vi = VerificationImage.builder()
                .userChallenge(userChallenge)
                .imageUrl(imageUrl)
                .uploadedAt(LocalDateTime.now())
                .build();

        verificationImageRepository.save(vi);

        userChallenge.increaseProgressDay();
        userChallengeRepository.save(userChallenge);

        return vi.getCertificationImageId();
    }

    @Transactional(readOnly = true)
    public List<VerificationImageReviewResponse> getLatestPendingImages(Long challengeId, Long reviewerId) {
        boolean isReviewerParticipating = userChallengeRepository
                .existsByUser_UserIdAndChallenge_ChallengeIdAndUserChallengeStatus(
                        reviewerId, challengeId, UserChallengeStatus.IN_PROGRESS
                );

        if (!isReviewerParticipating) {
            throw new IllegalStateException("해당 챌린지에 참가 중이 아닙니다.");
        }

        List<VerificationImage> pendingImages =
                verificationImageRepository.findTop3ByUserChallenge_Challenge_ChallengeIdAndVerificationStatusOrderByUploadedAtDesc(
                        challengeId, VerificationStatus.PENDING
                );

        if (pendingImages.isEmpty()) {
            throw new IllegalStateException("대기 중인 인증 이미지가 없습니다.");
        }

        return pendingImages
                .stream()
                .map(img -> new VerificationImageReviewResponse(
                        img.getCertificationImageId(),
                        img.getImageUrl(),
                        img.getVerificationStatus()
                ))
                .toList();
    }

    @Transactional
    public void reviewImages(Long challengeId, Long reviewerId, ReviewRequest req) {
        boolean isReviewerParticipating = userChallengeRepository
                .existsByUser_UserIdAndChallenge_ChallengeIdAndUserChallengeStatus(
                        reviewerId, challengeId, UserChallengeStatus.IN_PROGRESS
                );

        if (!isReviewerParticipating) {
            throw new IllegalStateException("현재 진행 중인 참가자가 아닙니다.");
        }

        if (req.getReviews() == null || req.getReviews().isEmpty()) {
            throw new IllegalArgumentException("검토할 이미지가 없습니다.");
        }

        List<Long> imageIds = req.getReviews().stream()
                .map(ReviewRequest.ReviewItem::getVerificationImageId)
                .toList();

        List<VerificationImage> images = verificationImageRepository.findAllById(imageIds);

        Map<Long, Boolean> decisionMap = req.getReviews().stream()
                .collect(Collectors.toMap(
                        ReviewRequest.ReviewItem::getVerificationImageId,
                        ReviewRequest.ReviewItem::isApproved
                ));

        for (VerificationImage img : images) {
            if (!img.getUserChallenge().getChallenge().getChallengeId().equals(challengeId)) {
                throw new IllegalStateException("해당 챌린지에 속하지 않는 이미지입니다: " + img.getCertificationImageId());
            }

            boolean approved = decisionMap.getOrDefault(img.getCertificationImageId(), false);
            if (approved) {
                img.markVerified();
            } else {
                img.revokeVerification();
            }
        }
    }

    @Transactional(readOnly = true)
    public List<VerificationImageResponse> getMyPendingImages(Long userId) {
        User me = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자 없음"));

        List<VerificationImage> all = verificationImageRepository.findAllByStatusWithChallengeAndCrew(VerificationStatus.PENDING);

        List<VerificationImage> filtered;
        if (me.isAdmin()) {
            filtered = all;
        } else {
            filtered = all.stream()
                    .filter(vi -> canModerate(me, vi))
                    .toList();
        }

        // ✅ 여기서 DTO로 변환까지 처리
        return filtered.stream()
                .map(VerificationImageResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VerificationImageResponse> getMyRejectedImages(Long userId) {
        User me = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자 없음"));

        List<VerificationImage> all = verificationImageRepository.findAllByStatusWithChallengeAndCrew(VerificationStatus.REJECTED);

        List<VerificationImage> filtered;
        if (me.isAdmin()) {
            filtered = all;
        } else {
            filtered = all.stream()
                    .filter(vi -> canModerate(me, vi))
                    .toList();
        }

        // ✅ 여기서 DTO로 변환까지 처리
        return filtered.stream()
                .map(VerificationImageResponse::from)
                .toList();
    }

    /** 승인 */
    public void approve(Long userId, Long imageId) {
        User me = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자 없음"));

        VerificationImage vi = verificationImageRepository.findById(imageId)
                .orElseThrow(() -> new EntityNotFoundException("인증 이미지 없음"));

        assertCanModerate(me, vi);
        vi.markVerified();
    }

    /** 거절 */
    public void reject(Long userId, Long imageId) {
        User me = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자 없음"));

        VerificationImage vi = verificationImageRepository.findById(imageId)
                .orElseThrow(() -> new EntityNotFoundException("인증 이미지 없음"));

        assertCanModerate(me, vi);
        vi.revokeVerification();

        var uc = vi.getUserChallenge();

        uc.changeStatus(UserChallengeStatus.FAILED);
        userChallengeRepository.save(uc);
    }

    private void assertCanModerate(User me, VerificationImage vi) {
        if (!canModerate(me, vi)) {
            throw new SecurityException("인증 이미지를 처리할 권한이 없습니다.");
        }
    }

    private boolean canModerate(User me, VerificationImage vi) {
        if (me.isAdmin()) return true;

        Challenge ch = vi.getUserChallenge().getChallenge();
        // PUBLIC: 관리자만 가능
        if (ch.getChallengeScope() == ChallengeScope.PUBLIC) {
            return false;
        }
        // CREW (또는 팀 범위): 해당 크루의 OWNER여야 가능
        if (ch.getCrew() != null) {
            return userCrewRepository.existsByUser_UserIdAndCrew_CrewIdAndUserCrewRole(
                    me.getUserId(), ch.getCrew().getCrewId(), UserCrewRole.OWNER
            );
        }
        return false;
    }
}
