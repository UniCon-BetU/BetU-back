package org.example.money.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TossPaymentResponse {
    private String paymentKey;
    private String orderId;
    private String status;        // ex) "DONE", "CANCELED"
    private Long totalAmount;
    private Long balanceAmount;
}