package org.example.money;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.money.dto.TossPaymentResponse;
import org.example.money.dto.PointChargeRequest;
import org.example.money.dto.PointChargeResponse;
import org.example.user.User;
import org.example.user.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PointService {

    private static final String STATUS_DONE = "DONE";

    private final TossPaymentsClient toss;
    private final UserRepository userRepository;
    private final PointPurchaseRepository pointPurchaseRepository;

    /** 토스 결제 승인 + 포인트 적립 (멱등/동시성 대응) */
    public PointChargeResponse confirmAndCredit(Long userId, PointChargeRequest req) {
        validateRequest(req);

        // 1) 트랜잭션 밖에서 승인 (네트워크 호출 중 DB 락 보유 방지)
        TossPaymentResponse res = confirmOutsideTx(req);

        // 2) 응답 교차 검증 (위·변조/오용 방지)
        validateTossResponseMatchesRequest(req, res);

        // 3) 트랜잭션 안에서 "유니크 제약 + 사용자 행 잠금"으로 동시성 제어
        return creditTx(userId, res);
    }

    private void validateRequest(PointChargeRequest req) {
        if (req.getAmount() == null || req.getAmount() <= 0) {
            throw new IllegalArgumentException("금액/포인트가 올바르지 않습니다.");
        }
        if (req.getOrderId() == null || req.getPaymentKey() == null) {
            throw new IllegalArgumentException("결제 식별자가 누락되었습니다.");
        }
    }

    private TossPaymentResponse confirmOutsideTx(PointChargeRequest req) {
        try {
            // Idempotency-Key로 orderId 사용 (Toss 중복 호출 방지)
            return toss.confirm(req.getPaymentKey(), req.getOrderId(), req.getAmount(), req.getOrderId());
        } catch (RuntimeException e) {
            // 이미 승인된 결제라면 조회로 동기화
            if (e.getMessage() != null && e.getMessage().contains("ALREADY_PROCESSED_PAYMENT")) {
                return toss.getPayment(req.getPaymentKey());
            }
            throw e;
        }
    }

    private void validateTossResponseMatchesRequest(PointChargeRequest req, TossPaymentResponse res) {
        if (!req.getOrderId().equals(res.getOrderId())) {
            throw new SecurityException("주문 식별자가 일치하지 않습니다.");
        }
        if (!req.getPaymentKey().equals(res.getPaymentKey())) {
            throw new SecurityException("결제 키가 일치하지 않습니다.");
        }
        // 승인 상태 확인
        if (!STATUS_DONE.equals(res.getStatus())) {
            throw new IllegalStateException("결제가 승인 상태(DONE)가 아닙니다. status=" + res.getStatus());
        }
        // 금액 일치 확인: 최초 승인 적립은 totalAmount 기준
        if (!Objects.equals(res.getTotalAmount(), req.getAmount())) {
            throw new SecurityException("승인 금액(totalAmount)이 요청 금액과 일치하지 않습니다.");
        }
    }

    /** 실제 적립(트랜잭션 내부): 유니크 제약 + 사용자 행 잠금으로 중복/경합 방지 */
    @Transactional
    protected PointChargeResponse creditTx(Long userId, TossPaymentResponse res) {
        try {
            // (A) 사용자 행 비관적 락 -> 포인트 합산 충돌 방지
            User user = userRepository.findByIdForUpdate(userId)
                    .orElseThrow(() -> new EntityNotFoundException("사용자 없음"));

            // (B) 먼저 구매 이력 저장(UNIQUE 제약으로 멱등 보장)
            PointPurchase purchase = PointPurchase.builder()
                    .user(user)
                    .amount(res.getTotalAmount())      // 결제 총 승인 금액
                    .pointAmount(res.getTotalAmount())  // 포인트 적립 금액 = totalAmount
                    .paymentKey(res.getPaymentKey())
                    .orderId(res.getOrderId())
                    .status(PointPurchase.Status.APPROVED)
                    .build();
            pointPurchaseRepository.save(purchase); // 중복이면 여기서 DataIntegrityViolationException

            // (C) 포인트 적립 (동일 트랜잭션 내 원자성)
            user.addPoint(res.getTotalAmount());

            return new PointChargeResponse(res.getTotalAmount(), user.getPoint());

        } catch (DataIntegrityViolationException dup) {
            // (D) paymentKey/orderId UNIQUE 충돌 → 이미 처리됨(멱등)
            User u = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("사용자 없음"));
            return new PointChargeResponse(0L, u.getPoint());
        }
    }
}