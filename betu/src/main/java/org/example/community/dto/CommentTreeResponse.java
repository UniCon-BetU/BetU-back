package org.example.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.community.entity.Comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentTreeResponse {

    private Long commentId;
    private Long userId;
    private String userName;
    private String content;        // 삭제 시 마스킹된 내용
    private int likeCount;
    private boolean likedByMe;     // 현재 사용자 기준
    private boolean deleted;       // ✅ 소프트 삭제 플래그 노출
    private LocalDateTime createdAt;

    @Builder.Default
    private List<CommentTreeResponse> replies = new ArrayList<>();

    public static CommentTreeResponse of(Comment c, boolean likedByMe) {
        boolean isDeleted = c.isDeleted();
        String content = isDeleted ? "(삭제된 댓글입니다)" : c.getCommentContent();

        return CommentTreeResponse.builder()
                .commentId(c.getCommentId())
                .userId(c.getUser().getUserId())
                .userName(c.getUser().getUserName())
                .content(content)
                .likeCount(c.getLikeCount())
                .likedByMe(likedByMe)
                .deleted(isDeleted)
                .createdAt(c.getCreatedAt())
                .build();
    }
}
