package org.example.challenge.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.community.entity.Post;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long challengeImageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    @Setter
    private Challenge challenge;

    @Column(nullable = false, length = 1000)
    private String imageUrl;

    private Integer sortOrder;     // 노출 순서 (0,1,2…)

    private Boolean isThumbnail;
}
