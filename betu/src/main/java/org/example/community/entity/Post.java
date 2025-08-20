package org.example.community.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.crew.entity.Crew;
import org.example.user.User;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @ManyToOne(optional = true)
    @JoinColumn(name = "crew_Id", nullable = true)
    private Crew crew;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)   // 작성자 필수
    private User user;

    @Lob
    private String postTitle;

    @Lob
    private String postContent;

    @Column(nullable = false)
    private int postLikeCnt = 0;

    public void increaseLike() { this.postLikeCnt++; }
    public void decreaseLike() { if (this.postLikeCnt > 0) this.postLikeCnt--; }
}
