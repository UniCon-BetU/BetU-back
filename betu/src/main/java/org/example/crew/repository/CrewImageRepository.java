package org.example.crew.repository;

import org.example.crew.entity.CrewImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CrewImageRepository extends JpaRepository<CrewImage, Long> {
    // 썸네일(true) 중 sortOrder 오름차순 첫 번째
    Optional<CrewImage> findFirstByCrew_CrewIdAndIsThumbnailTrueOrderBySortOrderAsc(Long crewId);

    // 썸네일 없으면 전체 중 sortOrder 오름차순 첫 번째
    Optional<CrewImage> findFirstByCrew_CrewIdOrderBySortOrderAsc(Long crewId);

    // (필요 시) 전체 이미지
    List<CrewImage> findByCrew_CrewIdOrderBySortOrderAsc(Long crewId);

    void deleteByCrew_CrewId(Long crewId);

}
