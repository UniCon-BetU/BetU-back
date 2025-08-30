package org.example.challenge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.challenge.entity.Challenge;
import org.example.challenge.entity.ChallengeScope;
import org.example.challenge.entity.ChallengeTag;
import org.example.challenge.entity.ChallengeType;
import org.example.crew.entity.Crew;
import org.example.user.User;

import java.time.LocalDate;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeCreateRequest {

    @NotNull
    private ChallengeScope challengeScope;

    private Long crewId;

    // 작성자(선택)
    private Long creatorId;

    private Set<ChallengeTag> challengeTags;

    private Set<String> customTags;

    @NotNull
    private ChallengeType challengeType;

    @NotBlank
    private String challengeName;

    @NotBlank
    private String challengeDescription;

    private int challengeDuration;


    public Challenge toEntity(Crew crew, User creator, ChallengeScope finalScope) {
        validateScopeGroupConsistency(challengeScope, crew != null);
        return Challenge.builder()
                .challengeScope(finalScope)
                .crew(crew)
                .creator(creator)
                .tags(challengeTags == null ? Set.of() : challengeTags)
                .customTags(customTags == null ? Set.of() : customTags)
                .challengeType(challengeType)
                .challengeName(challengeName)
                .challengeDescription(challengeDescription)
                .challengeDuration(challengeDuration)
                .challengeLikeCnt(0)
                .challengeParticipantCnt(0)
                .build();
    }

    private static void validateScopeGroupConsistency(ChallengeScope scope, boolean hasGroup) {
        if (scope == ChallengeScope.CREW && !hasGroup) {
            throw new IllegalStateException("GROUP 스코프는 group이 필수입니다.");
        }
        if (scope == ChallengeScope.PUBLIC && hasGroup) {
            throw new IllegalStateException("PUBLIC 스코프는 group을 가질 수 없습니다.");
        }
    }
}


