package org.example.email;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerificationCode, Long> {
    Optional<EmailVerificationCode> findTopByUserIdOrderByIdDesc(Long userId);
    void deleteByUserId(Long userId);
}
