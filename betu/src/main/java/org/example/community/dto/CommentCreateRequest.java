package org.example.community.dto;

import lombok.Getter;

@Getter
public class CommentCreateRequest {
    private Long postId;
    private String content;
}
