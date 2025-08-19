package org.example.challenge.repository;

import org.example.challenge.entity.Challenge;
import org.example.challenge.entity.ChallengeTag;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    @Query("select distinct c from Challenge c left join fetch c.crew")
    List<Challenge> findAllWithCrew();

    @Query("select c from Challenge c left join fetch c.crew order by c.challengeParticipantCnt desc")
    List<Challenge> findTop3ByOrderByChallengeParticipantCntDesc();

    @Query("select c from Challenge c left join fetch c.crew where c.challengeId = :challengeId")
    Optional<Challenge> findWithCrewByChallengeId(@Param("challengeId") Long challengeId);

    @Query("select distinct c from Challenge c left join fetch c.crew where c.challengeId in :ids")
    List<Challenge> findAllWithCrewByIdIn(@Param("ids") List<Long> ids);

    @Query("select distinct c from Challenge c left join fetch c.crew join c.tags t where t in :tags")
    List<Challenge> findByAnyTagsWithCrew(@Param("tags") Set<ChallengeTag> tags);

    @Query("select distinct c from Challenge c left join fetch c.crew where lower(c.challengeName) like lower(concat('%', :kw, '%')) or lower(c.challengeDescription) like lower(concat('%', :kw, '%'))")
    List<Challenge> searchWithCrew(@Param("kw") String keyword);
}
