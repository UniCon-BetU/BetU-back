package org.example.challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.challenge.entity.ChallengeScope;
import org.example.challenge.entity.ChallengeTag;
import org.example.challenge.entity.ChallengeType;
import org.example.crew.entity.Crew;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Getter
@AllArgsConstructor
public class ChallengeResponse {
    private Long challengeId;
    private ChallengeScope challengeScope;
    private Crew crew;
    private Set<ChallengeTag> challengeTags;
    private Set<String> customTags;
    private ChallengeType challengeType;
    private String challengeName;
    private String challengeDescription;
    private LocalDate challengeStartDate;
    private LocalDate challengeEndDate;
    private int challengeBetAmount;
    private long favoriteCount;
    private long participantCount;
    private String imageUrl;

}
