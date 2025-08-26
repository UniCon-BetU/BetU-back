package org.example.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String userName;
    private String userEmail;
    private String userPassword;

    @Column(nullable = false)
    private Long point = 0L;   // 기본 0 포인트

    private String refreshToken;
    // 프로필 이미지, 레벨 (도전해서 얻은 포인트 누적)
    // 나이

    public void updateRefreshToken(String newRefreshToken){
        this.refreshToken = newRefreshToken;
    }

    public void updatePassword(String newPassword) {
        this.userPassword = newPassword;
    }

    public void addPoint(long amount) {
        this.point += amount;
    }

    public void subtractPoint(long amount) {
        if (this.point < amount) {
            throw new IllegalStateException("포인트 부족");
        }
        this.point -= amount;
    }

    /** 현재 보유 포인트 확인 */
    public long getCurrentPoint() {
        return this.point;
    }

    /** 테스트/관리자용 포인트 충전 (제한 없이 더함) */
    public void forceSetPoint(long newPoint) {
        if (newPoint < 0) {
            throw new IllegalArgumentException("포인트는 음수가 될 수 없습니다.");
        }
        this.point = newPoint;
    }
}
