package org.example.community.repository;

import org.example.community.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByCrew_CrewIdOrderByPostIdDesc(Long crewId);
    List<Post> findByCrewIsNullOrderByPostIdDesc(); // crewId가 null인 경우

    List<Post> findAllByOrderByPostIdDesc();

    List<Post> findByCrew_CrewIdAndPostLikeCntGreaterThanEqualOrderByPostIdDesc(Long crewId, int minLike);

    List<Post> findByCrewIsNullAndPostLikeCntGreaterThanEqualOrderByPostIdDesc(int minLike);

    List<Post> findByPostTitleContainingIgnoreCaseOrPostContentContainingIgnoreCaseOrderByPostIdDesc(
            String titleKeyword, String contentKeyword
    );
}
