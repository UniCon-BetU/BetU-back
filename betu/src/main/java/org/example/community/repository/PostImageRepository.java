package org.example.community.repository;

import org.example.community.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {
    void deleteByPost_PostId(Long postId);

    List<PostImage> findByPost_PostIdOrderBySortOrderAsc(Long postId);
}
