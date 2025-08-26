package org.example.verification_image;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.challenge.entity.UserChallenge;
import org.example.user.User;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VerificationImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long certificationImageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_challenge_id", nullable = false)
    private UserChallenge userChallenge;

    @Column(nullable = false)
    private boolean certified = false;

    private String imageUrl;
    private LocalDateTime uploadedAt;

    public void markCertified() {
        this.certified = true;
    }

    public void revokeCertification() {
        this.certified = false;
    }
}
