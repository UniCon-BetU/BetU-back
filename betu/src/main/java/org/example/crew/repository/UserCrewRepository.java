package org.example.crew.repository;

import org.example.crew.entity.Crew;
import org.example.crew.entity.UserCrew;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserCrewRepository extends JpaRepository<UserCrew, Long> {
    Optional<UserCrew> findByUser_UserIdAndCrew_CrewId(Long userId, Long groupId);

    List<UserCrew> findByCrew_CrewId(Long groupId);

    boolean existsByUser_UserIdAndCrew_CrewId(Long userId, Long groupId);

    @Query("SELECT uc.crew FROM UserCrew uc WHERE uc.user.userId = :userId")
    List<Crew> findCrewsByUserId(@Param("userId") Long userId);
}
