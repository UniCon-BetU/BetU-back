package org.example.challenge.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.user.User;

@Entity
@NoArgsConstructor
@Getter
public class ChallengeLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(name="challenge_id", nullable=false)
    private Long challengeId;

    public ChallengeLike(Long userId, Long challengeId) {
        this.userId = userId;
        this.challengeId = challengeId;
    }
}
