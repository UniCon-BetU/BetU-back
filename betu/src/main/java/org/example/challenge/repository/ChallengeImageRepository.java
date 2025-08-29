package org.example.challenge.repository;

import org.example.challenge.entity.ChallengeImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChallengeImageRepository extends JpaRepository<ChallengeImage, Long> {
    Optional<ChallengeImage> findTopByChallenge_ChallengeIdOrderBySortOrderAsc(Long challengeId);

    List<ChallengeImage> findByChallenge_ChallengeIdOrderBySortOrderAsc(Long challengeId);

}
