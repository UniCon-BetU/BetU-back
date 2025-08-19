package org.example.verification_image;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VerificationImageRepository extends JpaRepository<VerificationImage, Long> {
    void deleteAllByUserChallenge_Challenge_ChallengeId(Long challengeId);
    List<VerificationImage> findByUserChallenge_Challenge_ChallengeId(Long challengeId);
}
