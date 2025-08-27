package org.example.community.dto;

import lombok.Getter;

@Getter
public class PostReportCreateRequest {
    private String reason; // null 가능하면 서버에서 빈 문자열로 정리해도 됨
}
