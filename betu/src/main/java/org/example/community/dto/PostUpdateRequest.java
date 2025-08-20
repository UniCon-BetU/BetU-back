package org.example.community.dto;

import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class PostUpdateRequest {
    private String title;
    private String content;                 // 본문 수정
}