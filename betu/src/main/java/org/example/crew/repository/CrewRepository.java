package org.example.crew.repository;

import org.example.crew.entity.Crew;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrewRepository extends JpaRepository<Crew, Long> {
    boolean existsByCrewCode(String code);

    List<Crew> findByCrewNameContainingIgnoreCase(String keyword);
}
