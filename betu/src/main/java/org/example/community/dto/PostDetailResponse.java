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
    private String authorName;
    private String postTitle;
    private String postContent;
    private int postLikeCnt;
    private List<String> imageUrls;
    private List<CommentTreeResponse> commentTree;

    private boolean liked;
}
