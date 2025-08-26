package org.example.community.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @ManyToOne
    @JoinColumn(name = "postId")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)   // 작성자 필수
    private User user;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "parent_id")
//    private Comment parent;
//
//    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
//    @OrderBy("commentId ASC")
//    private List<Comment> children = new ArrayList<>();

    private String commentContent;

    private LocalDateTime createdAt;

//    public void addChild(Comment child) {
//        children.add(child);
//        child.parent = this;
//    }
//
//    @PrePersist
//    public void onCreate() {
//        this.createdAt = LocalDateTime.now();
//    }
}
