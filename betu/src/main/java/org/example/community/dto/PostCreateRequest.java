package org.example.community.dto;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
public class PostCreateRequest {
    private Long crewId;
    private String content;
    // 다중 이미지 업로드
    private List<MultipartFile> images;
}