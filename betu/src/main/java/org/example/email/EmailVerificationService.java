package org.example.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.user.User;
import org.example.user.UserRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    private static final Duration TTL = Duration.ofMinutes(10);
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);
    private static final int MAX_ATTEMPTS = 5;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(); // 해시용

    /** 회원가입 직후 호출: 코드 생성+저장+발송 */
    @Transactional
    public void sendSignupCode(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        String email = user.getUserEmail();

        // 기존 미사용 코드가 있어도 새로 재발급(쿨다운만 체크)
        emailVerificationRepository.findTopByUserIdOrderByIdDesc(userId).ifPresent(prev -> {
            if (prev.getConsumedAt() == null && prev.getLastSentAt() != null) {
                if (prev.getLastSentAt().isAfter(Instant.now().minus(RESEND_COOLDOWN))) {
                    throw new IllegalStateException("잠시 후 다시 요청해주세요.");
                }
            }
        });

        String code = generate6();
        EmailVerificationCode evc = EmailVerificationCode.builder()
                .userId(userId)
                .email(email)
                .codeHash(encoder.encode(code))
                .expiresAt(Instant.now().plus(TTL))
                .attempts(0)
                .maxAttempts(MAX_ATTEMPTS)
                .lastSentAt(Instant.now())
                .build();
        emailVerificationRepository.save(evc);

        sendMail(email, "[BetU] 이메일 인증번호", mailBody(code));
    }

    /** 인증번호 검증: 성공 시 사용자 활성화 */
    @Transactional
    public void verifySignupCode(Long userId, VerificationCodeRequest verificationCodeRequest) {
        String code = verificationCodeRequest.getCode();
        log.info("📩 verifySignupCode called for userId={}, inputCode={}", userId, code);
        EmailVerificationCode evc = emailVerificationRepository.findTopByUserIdOrderByIdDesc(userId)
                .orElseThrow(() -> new IllegalArgumentException("인증 요청이 없습니다."));

        if (evc.getConsumedAt() != null) throw new IllegalStateException("이미 인증 완료되었습니다.");
        if (Instant.now().isAfter(evc.getExpiresAt())) throw new IllegalStateException("인증번호가 만료되었습니다.");

        // 시도 횟수 초과
        if (evc.getAttempts() >= evc.getMaxAttempts()) {
            throw new IllegalStateException("인증번호 입력 횟수를 초과했습니다.");
        }

        // 우선 시도 증가
        evc.setAttempts(evc.getAttempts() + 1);

        if (!encoder.matches(code, evc.getCodeHash())) {
            // 틀린 경우에도 증가된 attempts 저장
            emailVerificationRepository.save(evc);
            throw new IllegalArgumentException("인증번호가 올바르지 않습니다.");
        }

        // 성공: 소비 처리
        evc.setConsumedAt(Instant.now());
        emailVerificationRepository.save(evc);

        // 유저 활성화
        User user = userRepository.findById(userId).orElseThrow();
        user.isEmailVerified();
    }

    // ===== helpers =====

    private String generate6() {
        int n = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return String.valueOf(n);
    }

    private String mailBody(String code) {
        return """
      안녕하세요! 아래 인증번호를 입력해 이메일 인증을 완료해주세요.

      인증번호: %s

      유효기간: 10분
      """.formatted(code);
    }

    private void sendMail(String to, String subject, String body) {
        var msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);
        mailSender.send(msg);
    }
}
