package org.example.money;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.money.dto.TossPaymentResponse;
import org.example.money.dto.PointChargeRequest;
import org.example.money.dto.PointChargeResponse;
import org.example.user.User;
import org.example.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {

    private final TossPaymentsClient toss;
    private final UserRepository userRepository;
    private final PointPurchaseRepository pointPurchaseRepository;

    /** 토스 결제 승인 후 포인트 적립 (멱등/중복 적립 방지) */
    @Transactional
    public PointChargeResponse confirmAndCredit(Long userId, PointChargeRequest req) {
        // 0) 기본 검증
        if (req.getAmount() == null || req.getAmount() <= 0) {
            throw new IllegalArgumentException("금액/포인트가 올바르지 않습니다.");
        }

        // 1) 이미 처리된 paymentKey면 멱등 처리(2번 적립 금지)
        if (pointPurchaseRepository.findByPaymentKey(req.getPaymentKey()).isPresent()) {
            // 이미 적립된 건이므로 현재 유저 포인트만 리턴
            User u = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("사용자 없음"));
            return new PointChargeResponse(0L, u.getPoint());
        }

        // 2) 토스 결제 승인 (Idempotency-Key = orderId)
        TossPaymentResponse res;
        try {
            res = toss.confirm(req.getPaymentKey(), req.getOrderId(), req.getAmount(), req.getOrderId());
        } catch (RuntimeException e) {
            // 이미 승인된 경우 동기화
            if (e.getMessage() != null && e.getMessage().contains("ALREADY_PROCESSED_PAYMENT")) {
                res = toss.getPayment(req.getPaymentKey());
            } else {
                throw e;
            }
        }

        // 3) 유저 포인트 적립
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자 없음"));
        user.addPoint(req.getAmount());

        // 4) 구매 이력 저장 (중복 적립 방지용)
        PointPurchase purchase = PointPurchase.builder()
                .user(user)
                .amount(req.getAmount())
                .pointAmount(req.getAmount())
                .paymentKey(res.getPaymentKey())
                .orderId(res.getOrderId())
                .status(PointPurchase.Status.APPROVED)
                .build();
        pointPurchaseRepository.save(purchase);

        return new PointChargeResponse(req.getAmount(), user.getPoint());
    }
}