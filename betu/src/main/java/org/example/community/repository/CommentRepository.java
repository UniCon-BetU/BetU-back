package org.example.community.repository;

import org.example.community.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Iterable<Object> findByPost_PostId(Long postId, PageRequest of);
    Page<Comment> findByPost_PostId(Long postId, Pageable pageable);
    List<Comment> findByPost_PostId(Long postId);
    void deleteByPost_PostId(Long postId);

    @EntityGraph(attributePaths = {"user", "parent"})
    List<Comment> findByPost_PostIdOrderByCommentIdAsc(Long postId);
}
