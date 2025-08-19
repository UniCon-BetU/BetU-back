package org.example.community.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.user.User;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postLikeId;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(name="post_id", nullable=false)
    private Long postId;

    public PostLike(Long userId, Long postId) {
        this.userId = userId;
        this.postId = postId;
    }
}
