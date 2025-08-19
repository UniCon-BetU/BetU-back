package org.example.community.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostSummaryResponse {
    private Long postId;
    private Long crewId;
    private Long authorId;
    private String authorName;     // 필요 없으면 제거
    private String preview;        // 앞부분만 잘라서
    private Integer likeCount;
    private Integer commentCount;
    private String thumbnailUrl;   // 첫 이미지 URL (없으면 null)
}
