package org.example.verification_image;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {
    private List<ReviewItem> reviews;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewItem {
        private Long verificationImageId;
        private boolean approved; // true면 APPROVED, false면 REJECTED
    }
}