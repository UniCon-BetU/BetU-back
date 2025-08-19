package org.example.challenge.repository;

import org.example.challenge.entity.ChallengeLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChallengeLikeRepository extends JpaRepository<ChallengeLike, Long> {
    boolean existsByUserIdAndChallengeId(Long userId, Long challengeId);
    void deleteByUserIdAndChallengeId(Long userId, Long challengeId);

    void deleteAllByChallengeId(Long challengeId);
    List<ChallengeLike> findByUserId(Long userId);
}