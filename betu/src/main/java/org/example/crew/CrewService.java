package org.example.crew;

import lombok.RequiredArgsConstructor;
import org.example.crew.dto.CrewCreateRequest;
import org.example.crew.dto.CrewJoinRequest;
import org.example.crew.dto.CrewResponse;
import org.example.crew.entity.Crew;
import org.example.crew.entity.UserCrew;
import org.example.crew.entity.UserCrewRole;
import org.example.crew.repository.CrewRepository;
import org.example.crew.repository.UserCrewRepository;
import org.example.user.User;
import org.example.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class CrewService {
    private final CrewRepository crewRepository;
    private final UserRepository userRepository;
    private final UserCrewRepository userCrewRepository;

    // 그룹 생성
    // isPublic: true - code null, false - code 생성
    // 그룹 생성한 사람 OWNER 설정
    @Transactional
    public CrewResponse createCrew(Long userId, CrewCreateRequest req) {
        User user = userRepository.getReferenceById(userId);
        boolean isPublic = Boolean.TRUE.equals(req.getIsPublic());

        String code = isPublic ? null : generateUniqueCrewCode();
        Crew group = crewRepository.save(new Crew(req.getCrewName(), code, isPublic));

        userCrewRepository.save(new UserCrew(user, group, UserCrewRole.OWNER));

        return toResponse(group, UserCrewRole.OWNER);
    }

    private String generateUniqueCrewCode() {
        final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        while (true) {
            StringBuilder sb = new StringBuilder(8);
            for (int i=0;i<8;i++) sb.append(alphabet.charAt(rnd.nextInt(alphabet.length())));
            String code = sb.toString();
            if (!crewRepository.existsByCrewCode(code)) return code;
        }
    }

    // 그룹 참가
    // 비공개 - 그룹코드 확인
    @Transactional
    public CrewResponse joinCrew(Long userId, CrewJoinRequest req) {
        Crew crew = crewRepository.findById(req.getCrewId())
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        if (!Boolean.TRUE.equals(crew.getCrewIsPublic())) {
            if (req.getCrewCode() == null || !req.getCrewCode().equals(crew.getCrewCode())) {
                throw new IllegalArgumentException("올바른 그룹 코드가 필요합니다.");
            }
        }

        User user = userRepository.getReferenceById(userId);
        if (!userCrewRepository.existsByUser_UserIdAndCrew_CrewId(userId, crew.getCrewId())) {
            userCrewRepository.save(new UserCrew(user, crew, UserCrewRole.MEMBER));
        }

        UserCrewRole role = userCrewRepository.findByUser_UserIdAndCrew_CrewId(userId, crew.getCrewId())
                .map(UserCrew::getUserCrewRole)
                .orElse(UserCrewRole.MEMBER);

        return toResponse(crew, role);
    }

    // 그룹 삭제
    // OWNER만 가능
    @Transactional
    public void deleteCrew(Long userId, Long groupId) {
        Crew group = crewRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        UserCrew ug = userCrewRepository.findByUser_UserIdAndCrew_CrewId(userId, groupId)
                .orElseThrow(() -> new IllegalStateException("그룹에 속해있지 않습니다."));
        if (ug.getUserCrewRole() != UserCrewRole.OWNER) {
            throw new SecurityException("OWNER만 그룹을 삭제할 수 있습니다.");
        }

        List<UserCrew> links = userCrewRepository.findByCrew_CrewId(groupId);
        userCrewRepository.deleteAll(links);
        crewRepository.delete(group);
    }

    // 전체 그룹 조회
    @Transactional(readOnly = true)
    public List<CrewResponse> getAllCrews() {
        return crewRepository.findAll().stream()
                .map(g -> toResponse(g, null))
                .toList();
    }

    // 내가 참여한 그룹 조회
    public List<CrewResponse> getMyCrews(Long userId) {
        List<Crew> groups = userCrewRepository.findCrewsByUserId(userId);

        Map<Long, UserCrewRole> roleMap = userCrewRepository.findAll().stream()
                .filter(ug -> ug.getUser().getUserId().equals(userId))
                .collect(java.util.stream.Collectors.toMap(
                        ug -> ug.getCrew().getCrewId(),
                        UserCrew::getUserCrewRole
                ));
        return groups.stream()
                .map(g -> toResponse(g, roleMap.get(g.getCrewId())))
                .toList();
    }

    // 이름으로 그룹 검색
    public List<CrewResponse> searchCrewsByName(String keyword) {
        List<Crew> groups = crewRepository.findByCrewNameContainingIgnoreCase(keyword);

        return groups.stream()
                .map(g -> toResponse(g, null))
                .toList();
    }



    private CrewResponse toResponse(Crew g, UserCrewRole role) {
        return new CrewResponse(g.getCrewId(), g.getCrewName(), g.getCrewCode(), g.getCrewIsPublic(), role);
    }

}
