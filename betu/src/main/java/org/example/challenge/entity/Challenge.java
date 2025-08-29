package org.example.challenge.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.challenge.dto.ChallengeCreateRequest;
import org.example.crew.entity.Crew;
import org.example.user.User;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long challengeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengeScope challengeScope;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crew_id", nullable = true)
    private Crew crew;

    @ManyToOne(fetch = FetchType.LAZY, optional = true) // 선택적
    @JoinColumn(name = "creator_id", nullable = true)   // DB 컬럼도 NULL 허용
    private User creator;

    @ElementCollection
    @CollectionTable(name = "challenge_tags",
            joinColumns = @JoinColumn(name = "challenge_id"))
    @Column(name = "tag", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private Set<ChallengeTag> tags = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "challenge_custom_tags",
            joinColumns = @JoinColumn(name = "challenge_id"))
    @Column(name = "custom_tag", nullable = false, length = 50)
    private Set<String> customTags = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private ChallengeType challengeType;

    private String challengeName;
    private String challengeDescription;
    private int challengeDuration;
    private int challengeLikeCnt;
    private int challengeParticipantCnt;

    public void increaseLikeCount() {
        this.challengeLikeCnt++;
    }

    public void decreaseLikeCount() {
        if (this.challengeLikeCnt > 0) {
            this.challengeLikeCnt--;
        }
    }

    public void increaseParticipantCount() {
        this.challengeParticipantCnt++;
    }

}
