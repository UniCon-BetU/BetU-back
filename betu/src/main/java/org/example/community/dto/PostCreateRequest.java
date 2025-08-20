package org.example.community.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@NoArgsConstructor
@Data
public class PostCreateRequest {
    private Long crewId;
    private String title;
    private String content;
}