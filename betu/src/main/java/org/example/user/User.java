package org.example.user;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    private String refreshToken;
    // 프로필 이미지, 레벨 (도전해서 얻은 포인트 누적)
    // 나이

    public void updateRefreshToken(String newRefreshToken){
        this.refreshToken = newRefreshToken;
    }

    public void updatePassword(String newPassword) {
        this.userPassword = newPassword;
    }



}
