package org.example.community.repository;

import org.example.community.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByCrew_CrewIdOrderByPostIdDesc(Long crewId, Pageable pageable);
}
