package org.example.challenge.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.user.User;

@Entity
@NoArgsConstructor
@Getter
public class UserChallenge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userChallengeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserChallengeRole userChallengeRole; // CREATOR / PARTICIPANT

    @Enumerated(EnumType.STRING)
    private UserChallengeStatus userChallengeStatus;

    private long progressDay;

    public UserChallenge(User user, Challenge challenge, UserChallengeRole userChallengeRole) {
        this.user = user;
        this.challenge = challenge;
        this.userChallengeRole = userChallengeRole;
        this.userChallengeStatus = UserChallengeStatus.NOT_STARTED;
        this.progressDay = 0;
    }

    public void increaseProgressDay() {
        this.progressDay++;
    }

    public int getProgressPercent() {
        long durationDays = challenge.getDurationDays(); // (끝-시작)+1 기준
        if (durationDays <= 0) return 0;
        long capped = Math.min(progressDay, durationDays); // 과다 카운트 방지
        int pct = (int) Math.floor((capped * 100.0) / durationDays);
        if (pct < 0) return 0;
        return Math.min(pct, 100);
    }

    public void changeStatus(UserChallengeStatus userChallengeStatus) {
        this.userChallengeStatus = userChallengeStatus;
    }
}
