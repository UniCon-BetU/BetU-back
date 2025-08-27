package org.example.community.dto;

import lombok.Getter;

@Getter
public class ReplyCommentCreateRequest {
    private Long commentId;
    private String commentContent;
}
