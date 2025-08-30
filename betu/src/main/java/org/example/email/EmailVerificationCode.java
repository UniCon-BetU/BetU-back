package org.example.email;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
public class EmailVerificationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;                  // 가입 직후 유저 ID

    @Column(nullable = false, length = 320)
    private String email;                 // 인증 대상 이메일

    @Column(nullable = false, length = 100)
    private String codeHash;              // 평문 6자리 대신 해시(BCrypt 등)

    @Column(nullable = false)
    private Instant expiresAt;            // 유효기간 (예: 10분)

    @Column(nullable = false)
    private Integer attempts;             // 시도 횟수

    @Column(nullable = false)
    private Integer maxAttempts;          // 최대 시도 (예: 5)

    @Column
    private Instant consumedAt;           // 성공시 타임스탬프

    @Column
    private Instant lastSentAt;           // 최근 발송 시간(재전송 쿨다운)
}
