package org.example.community.repository;

import org.example.community.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    boolean existsByUser_UserIdAndComment_CommentId(Long userId, Long commentId);

    void deleteByUser_UserIdAndComment_CommentId(Long userId, Long commentId);

    long countByComment_CommentId(Long commentId);

    void deleteByComment_CommentId(Long commentId); // 필요시 사용

    @Query("""
        select cl.comment.commentId
        from CommentLike cl
        where cl.user.userId = :userId
          and cl.comment.commentId in :commentIds
    """)
    List<Long> findLikedCommentIdsByUser(Long userId, Collection<Long> commentIds);
}
