package org.example.challenge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.challenge.entity.Challenge;
import org.example.challenge.entity.ChallengeScope;
import org.example.challenge.entity.ChallengeTag;
import org.example.challenge.entity.ChallengeType;
import org.example.crew.entity.Crew;
import org.example.user.User;

import java.time.LocalDate;
import java.util.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeCreateRequest {

    @NotNull
    private ChallengeScope challengeScope; // PERSONAL or GROUP

    private Long crewId;

    // 작성자(선택)
    private Long creatorId;

    private Set<ChallengeTag> challengeTags;

    @NotNull
    private ChallengeType challengeType;

    @NotBlank
    private String challengeName;

    @NotBlank
    private String challengeDescription;

    private LocalDate challengeStartDate;

    private LocalDate challengeEndDate;

    private int challengeBetAmount;

    public Challenge toEntity(Crew crew, User creator) {
        validateScopeGroupConsistency(challengeScope, crew != null);
        return new Challenge(
                null,
                challengeScope,
                crew,      // null 가능
                creator,    // null 가능
                challengeTags,
                challengeType,
                challengeName,
                challengeDescription,
                challengeStartDate,
                challengeEndDate,
                challengeBetAmount,
                0,
                0
        );
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


