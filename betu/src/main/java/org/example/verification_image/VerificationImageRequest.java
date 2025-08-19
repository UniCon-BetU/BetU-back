package org.example.verification_image;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
public class VerificationImageRequest {
    private MultipartFile image;    // 인증 이미지
}