package org.example.money.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PointChargeResponse {
    private Long credited;   // 이번에 적립된 포인트
    private Long totalPoint; // 적립 후 보유 포인트
}
