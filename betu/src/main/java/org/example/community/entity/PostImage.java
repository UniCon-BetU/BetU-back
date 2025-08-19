package org.example.community.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostImage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postImageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @Setter
    private Post post;

    @Column(nullable = false, length = 1000)
    private String imageUrl;

    private Integer sortOrder;     // 노출 순서 (0,1,2…)

    private Boolean isThumbnail;   // 대표 이미지 여부
}
