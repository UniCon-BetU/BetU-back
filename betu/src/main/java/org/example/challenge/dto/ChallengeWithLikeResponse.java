package org.example.challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.challenge.entity.ChallengeScope;
import org.example.challenge.entity.ChallengeTag;
import org.example.challenge.entity.ChallengeType;
import org.example.crew.entity.Crew;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class ChallengeWithLikeResponse {
    private Long challengeId;
    private ChallengeScope challengeScope;
    private Crew crew;
    private Set<ChallengeTag> challengeTags;
    private Set<String> customTags;
    private ChallengeType challengeType;
    private String challengeName;
    private String challengeDescription;
    private int challengeDuration;
    private long favoriteCount;
    private long participantCount;
    private String imageUrl;
    private boolean liked;
}
