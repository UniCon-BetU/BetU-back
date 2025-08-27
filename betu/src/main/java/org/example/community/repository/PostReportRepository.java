package org.example.community.repository;

import org.example.community.entity.PostReport;
import org.example.community.entity.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostReportRepository extends JpaRepository<PostReport, Long> {
    boolean existsByReporter_UserIdAndPost_PostId(Long reporterId, Long postId);

    @Query("""
        select pr
          from PostReport pr
          join fetch pr.post p
          left join fetch p.crew c
          join fetch pr.reporter r
         where pr.status = :status
         order by pr.createdAt asc
    """)
    List<PostReport> findAllByStatusWithPostAndCrew(@Param("status") ReportStatus status);

    Optional<PostReport> findByReportId(Long id);
}
