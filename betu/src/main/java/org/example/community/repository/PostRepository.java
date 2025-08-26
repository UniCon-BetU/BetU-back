package org.example.community.repository;

import org.example.community.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByCrew_CrewIdOrderByPostIdDesc(Long crewId);
    List<Post> findByCrewIsNullOrderByPostIdDesc(); // crewId가 null인 경우

    List<Post> findAllByOrderByPostIdDesc();

    List<Post> findByCrew_CrewIdAndPostLikeCntGreaterThanEqualOrderByPostIdDesc(Long crewId, int minLike);

    List<Post> findByCrewIsNullAndPostLikeCntGreaterThanEqualOrderByPostIdDesc(int minLike);

    @Query("""
       SELECT p
         FROM Post p
        WHERE (
                (:crewId IS NULL AND p.crew IS NULL)  
             OR (:crewId IS NOT NULL AND p.crew.crewId = :crewId) 
              )
          AND (
               lower(cast(p.postTitle as string)) LIKE lower(concat('%', :q, '%'))
            OR lower(cast(p.postContent as string)) LIKE lower(concat('%', :q, '%'))
          )
        ORDER BY p.postId DESC
    """)
    List<Post> searchLatestByKeyword(@Param("crewId") Long crewId,
                                     @Param("q") String keyword);
}
