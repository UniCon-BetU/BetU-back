package org.example.challenge.repository;

import org.example.challenge.entity.UserChallenge;
import org.example.challenge.entity.UserChallengeRole;
import org.example.challenge.entity.UserChallengeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserChallengeRepository extends JpaRepository<UserChallenge, Long> {
    void deleteAllByChallenge_ChallengeId(Long challengeId);

    boolean existsByUser_UserIdAndChallenge_ChallengeIdAndUserChallengeRole(Long userId, Long challengeId, UserChallengeRole userChallengeRole);

    List<UserChallenge> findAllByUser_UserId(Long userId);

    Optional<UserChallenge> findByUser_UserIdAndChallenge_ChallengeId(Long userId, Long challengeId);


    @Query("""
        SELECT uc.user.userId, COUNT(uc) as challengeCount
        FROM UserChallenge uc
        WHERE uc.challenge.crew.crewId = :crewId
        GROUP BY uc.user.userId
        ORDER BY challengeCount DESC
    """)
    List<Object[]> findUserRankingByCrew(Long crewId);

}
