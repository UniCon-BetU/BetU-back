package org.example.verification_image;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.challenge.entity.UserChallenge;
import org.example.user.User;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long certificationImageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_challenge_id", nullable = false)
    private UserChallenge userChallenge;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING; // 기본값

    private String imageUrl;
    private LocalDateTime uploadedAt;

    public void markVerified() {
        this.verificationStatus = VerificationStatus.APPROVED;
    }

    public void revokeVerification() {
        this.verificationStatus = VerificationStatus.REJECTED;
    }
}
