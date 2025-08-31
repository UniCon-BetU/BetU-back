package org.example.verification_image;

import lombok.*;

@Getter
@AllArgsConstructor
public class VerificationImageReviewResponse {
    private Long id;
    private String imageUrl;
    private VerificationStatus status;
}
