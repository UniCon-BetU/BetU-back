package org.example.challenge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.challenge.entity.ChallengeScope;
import org.example.challenge.entity.ChallengeType;
import org.example.crew.entity.Crew;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeUpdateRequest {

    @NotNull
    private ChallengeScope challengeScope;

    // GROUP이면 필요, PERSONAL이면 null
    private Long crewId;

    // 작성자 변경을 허용하는 정책이라면 사용(선택)
    private Long creatorId;

    @NotNull
    private ChallengeType challengeType;

    @NotBlank
    private String challengeName;

    @NotBlank
    private String challengeDescription;


    private static void validateScopeGroupConsistency(ChallengeScope scope, boolean hasGroup) {
        if (scope == ChallengeScope.CREW && !hasGroup) {
            throw new IllegalStateException("GROUP 스코프는 group이 필수입니다.");
        }
        if (scope == ChallengeScope.PUBLIC && hasGroup) {
            throw new IllegalStateException("PUBLIC 스코프는 group을 가질 수 없습니다.");
        }
    }
}