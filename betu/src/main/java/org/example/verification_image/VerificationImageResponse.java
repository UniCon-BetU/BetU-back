package org.example.verification_image;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class VerificationImageResponse {
    private Long imageId;
    private String imageUrl;
    private LocalDateTime uploadedAt;
    private VerificationStatus status;
    private Long challengeId;
    private String challengeName;
    private Long crewId;
    private String crewName;
    private Long userId;
    private String userName;

    public static VerificationImageResponse from(VerificationImage vi) {
        var uc = vi.getUserChallenge();
        var ch = uc.getChallenge();
        var crew = ch.getCrew();
        var user = uc.getUser();
        return new VerificationImageResponse(
                vi.getCertificationImageId(),
                vi.getImageUrl(),
                vi.getUploadedAt(),
                vi.getVerificationStatus(),
                ch.getChallengeId(),
                ch.getChallengeName(),
                (crew != null ? crew.getCrewId() : null),
                (crew != null ? crew.getCrewName() : null),
                user.getUserId(),
                user.getUserName()
        );
    }
}