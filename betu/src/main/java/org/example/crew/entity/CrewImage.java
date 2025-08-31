package org.example.crew.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrewImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long crewImageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crew_id", nullable = false)
    @Setter
    private Crew crew;

    @Column(nullable = false, length = 1000)
    private String imageUrl;

    private Integer sortOrder;     // 노출 순서 (0,1,2…)

    private Boolean isThumbnail;
}

