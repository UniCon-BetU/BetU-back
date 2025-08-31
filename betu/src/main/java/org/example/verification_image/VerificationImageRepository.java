package org.example.verification_image;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VerificationImageRepository extends JpaRepository<VerificationImage, Long> {
    void deleteAllByUserChallenge_Challenge_ChallengeId(Long challengeId);

    @Query("""
        select vi
          from VerificationImage vi
          join fetch vi.userChallenge uc
          join fetch uc.challenge ch
          left join fetch ch.crew c
         where vi.verificationStatus = :status
         order by vi.uploadedAt asc
    """)
    List<VerificationImage> findAllByStatusWithChallengeAndCrew(@Param("status") VerificationStatus status);

    List<VerificationImage> findTop3ByUserChallenge_Challenge_ChallengeIdAndVerificationStatusOrderByUploadedAtDesc(Long challengeId, VerificationStatus verificationStatus);
}
