package org.example.money;

import lombok.RequiredArgsConstructor;
import org.example.money.dto.TossPaymentResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class TossPaymentsClient {
    private final WebClient tossWebClient;

    public TossPaymentResponse confirm(String paymentKey, String orderId, long amount, String idempotencyKey) {
        return tossWebClient.post()
                .uri("/v1/payments/confirm")
                .header("Idempotency-Key", idempotencyKey)       // 권장: orderId
                .bodyValue(Map.of("paymentKey", paymentKey, "orderId", orderId, "amount", amount))
                .retrieve()
                .onStatus(HttpStatusCode::isError, r ->
                        r.bodyToMono(String.class).map(msg -> new RuntimeException("Toss confirm error: " + msg)))
                .bodyToMono(TossPaymentResponse.class)
                .block();
    }

    public TossPaymentResponse getPayment(String paymentKey) {
        return tossWebClient.get()
                .uri("/v1/payments/{paymentKey}", paymentKey)
                .retrieve()
                .onStatus(HttpStatusCode::isError, r ->
                        r.bodyToMono(String.class).map(msg -> new RuntimeException("Toss getPayment error: " + msg)))
                .bodyToMono(TossPaymentResponse.class)
                .block();
    }
}
