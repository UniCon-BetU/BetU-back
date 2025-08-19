package org.example.community.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PostResponse {
    private Long postId;
    private Long groupId;
    private Long authorId;
    private String contentPreview; // 앞부분만
    private List<String> imageUrls; // 썸네일 1장만 내려도 OK
    private long commentCount;
    private LocalDateTime createdAt;
}
