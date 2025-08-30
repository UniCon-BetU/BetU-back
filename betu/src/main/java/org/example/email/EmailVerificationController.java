package org.example.email;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.user.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/email")
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;
    private final UserService userService; // 토큰에서 userId 뽑아오는 서비스라 가정

    /** 회원가입 직후: 인증번호 발송 */
    @PostMapping("/signup/send-code")
    public ResponseEntity<Void> sendCode(HttpServletRequest req) {
        Long userId = userService.getUserIdFromToken(req); // 또는 프런트에서 전달
        emailVerificationService.sendSignupCode(userId);
        return ResponseEntity.ok().build();
    }

    /** 인증번호 검증 */
    @PostMapping(value = "/signup/verify-code", consumes= MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> verifyCode(HttpServletRequest req, @Valid @RequestBody VerificationCodeRequest verificationCodeRequest) {
        Long userId = userService.getUserIdFromToken(req);
        log.info("verificationcode: {}", verificationCodeRequest.getCode());
        emailVerificationService.verifySignupCode(userId, verificationCodeRequest);
        return ResponseEntity.ok("인증이 성공했습니다 ✅");
    }
}