package org.example.money;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.money.dto.PointChargeRequest;
import org.example.money.dto.PointChargeResponse;
import org.example.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/points")
public class PointController {

    private final PointService pointService;
    private final UserService userService;

    @PostMapping("/charge/confirm")
    @Operation(summary = "포인트 충전 확정", description = "토스 결제 위젯 완료 후 paymentKey/orderId/amount를 넘겨 승인하고 포인트를 적립합니다.")
    public ResponseEntity<PointChargeResponse> confirmAndCredit(
            HttpServletRequest request,
            @RequestBody PointChargeRequest req
    ) {
        Long userId = userService.getUserIdFromToken(request);
        return ResponseEntity.ok(pointService.confirmAndCredit(userId, req));
    }
}