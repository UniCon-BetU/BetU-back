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
public class ChallengeDetailResponse {
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

    private boolean isParticipating; // 참가 여부
    private Integer progress;        // 진행률 (참여하지 않으면 null)

    private boolean liked;

    private List<String> imageUrls;
    private boolean todayVerified;

}
