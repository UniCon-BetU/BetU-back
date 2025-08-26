package org.example.money.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PointChargeRequest {
    private String paymentKey;
    private String orderId;
    private Long amount;       // 결제금액 (원)
}
