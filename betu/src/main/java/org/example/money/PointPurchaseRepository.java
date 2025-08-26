package org.example.money;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointPurchaseRepository extends JpaRepository<PointPurchase, Long> {
    Optional<PointPurchase> findByPaymentKey(String paymentKey);
}