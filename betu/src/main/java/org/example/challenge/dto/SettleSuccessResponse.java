package org.example.challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SettleSuccessResponse {
    private Long refundAmount;        // 환불(원)
    private Long bonusPointsCredited; // 적립 포인트
}
