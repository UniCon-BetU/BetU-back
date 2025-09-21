package org.example.money;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.user.User;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "point_purchase",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_point_purchase_payment_key", columnNames = "payment_key"),
                @UniqueConstraint(name = "uk_point_purchase_order_id",   columnNames = "order_id")
        })
public class PointPurchase {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pointPurchaseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    @Column(nullable=false)
    private Long amount;        // 결제 금액(원)

    @Column(nullable=false)
    private Long pointAmount;   // 적립 포인트

    @Column(length=100, unique = true, nullable=false)
    private String paymentKey;  // 중복 적립 방지

    @Column(length=100, nullable=false)
    private String orderId;     // 멱등키로도 활용

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private Status status;      // APPROVED, CANCELED

    public enum Status { APPROVED, CANCELED }

    public void markCanceled() { this.status = Status.CANCELED; }
}