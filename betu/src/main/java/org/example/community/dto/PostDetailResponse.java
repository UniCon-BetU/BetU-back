package org.example.community.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PostDetailResponse {
    private Long postId;
    private Long crewId;
    private Long authorId;
    private String authorName;// 필요 시
    private String title;
    private String content;
    private Integer likeCount;
    private List<String> imageUrls;        // 정렬된 이미지 URL 목록
    private List<CommentItem> comments;    // 댓글 목록
    private boolean liked;

    @Getter
    @AllArgsConstructor
    public static class CommentItem {
        private Long commentId;
        private Long userId;
        private String userName;           // 필요 시
        private String content;
    }
}
