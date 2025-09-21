package org.example.gpt.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.gpt.dto.ChatMessage;
import org.example.gpt.dto.ChatRequest;
import org.example.gpt.dto.ChatResponse;
import org.example.gpt.dto.ChallengeItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GptService {

    private final WebClient openAi;
    private final String model;
    private final int timeoutMs;
    private final ObjectMapper mapper = new ObjectMapper();

    public GptService(
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.model}") String model,
            @Value("${openai.timeout-ms}") int timeoutMs
    ) {
        this.model = model;
        this.timeoutMs = timeoutMs;
        this.openAi = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .build();
    }

    /* ================================
     * 1) 동기부여 문구 생성
     * ================================ */
    public String generateQuote(String prompt) {
        // 사용자가 준 프롬프트를 '맥락'으로 넘기고, 출력 형식/톤은 시스템에서 강제
        String userMessage = """
            오늘 날짜: %s
            사용자 맥락: %s

            조건:
            - 한국어로 1~2문장, 120자 이내, 이모지 0~1개
            - 과장 금지, 진부한 문구 피함
            - 사용자의 맥락과 자연스럽게 연결
            - 마지막 줄에 해시태그 1개 (영문 1~2단어)

            출력 포맷(문장만):
            문장
            #hashtag
            """.formatted(
                LocalDate.now(),
                (prompt == null || prompt.isBlank() ? "집중/꾸준함/자기계발" : prompt)
        );

        var messages = List.of(
                new ChatMessage("system",
                        "너는 간결하고 실용적인 동기부여 카피라이터다. " +
                                "클리셰를 피하고 현실적인 온도로 격려한다. " +
                                "요구한 출력 포맷만 반환하고, 불필요한 설명은 추가하지 마라."),
                new ChatMessage("user", userMessage)
        );

        var request = new ChatRequest(model, messages, 0.8, 120);

        try {
            ChatResponse res = openAi.post()
                    .uri("/chat/completions")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ChatResponse.class)
                    .timeout(java.time.Duration.ofMillis(timeoutMs))
                    .block();

            if (res != null && res.getChoices() != null && !res.getChoices().isEmpty()) {
                return res.getChoices().get(0).getMessage().getContent();
            }
        } catch (Exception e) {
            System.err.println("OpenAI(quote) 호출 실패: " + e.getMessage());
        }

        // 장애 시 폴백
        return """
            오늘의 한 걸음이 내일의 기준이 된다.
            #keepgoing
            """;
    }

    /* =====================================
     * 2) 챌린지 재랭킹: 후보(id,title) → Top-3 id
     * ===================================== */
    public List<Long> pickTop3ByPrompt(String userPrompt, List<ChallengeItem> items) {
        if (items == null || items.isEmpty()) return List.of();

        int batchSize = 120;  // 과도한 토큰 방지
        int perBatchTop = 5;

        if (items.size() > batchSize) {
            List<Long> pooled = new ArrayList<>();
            for (int i = 0; i < items.size(); i += batchSize) {
                List<ChallengeItem> batch = items.subList(i, Math.min(i + batchSize, items.size()));
                pooled.addAll(pickTopKOnce(userPrompt, batch, perBatchTop));
            }
            Map<Long, String> titleMap = items.stream()
                    .collect(Collectors.toMap(ChallengeItem::getChallengeId, ChallengeItem::getChallengeName, (a,b)->a));
            List<ChallengeItem> pooledItems = pooled.stream()
                    .distinct()
                    .map(id -> new ChallengeItem(id, titleMap.getOrDefault(id, "")))
                    .toList();
            return pickTopKOnce(userPrompt, pooledItems, 3);
        } else {
            return pickTopKOnce(userPrompt, items, 3);
        }
    }

    private List<Long> pickTopKOnce(String userPrompt, List<ChallengeItem> items, int topK) {
        String itemsJson = items.stream()
                .map(it -> String.format("{\"id\":%d,\"title\":\"%s\"}", it.getChallengeId(), escape(it.getChallengeName())))
                .collect(Collectors.joining(",", "[", "]"));

        String system = """
            너는 추천 랭커다. 주어진 후보 목록 중에서만 선택해라.
            존재하지 않는 항목을 만들지 말고, 오직 제공된 id만 사용해라.
            출력은 반드시 단 하나의 JSON만 반환한다.
            """;

        String user = """
            사용자의 의도(입력): %s

            후보 목록(JSON 배열):
            %s

            요구사항:
            1) 사용자의 의도와 가장 유사한 챌린지 상위 %d개를 선택한다.
            2) 각 항목은 {"id": number, "score": number(0~1), "reason": string(간단)} 형식이다.
            3) 최종 출력은 다음 한 개의 JSON만: {"items":[ ... ]}
            4) 제공된 후보 목록에 없는 id를 쓰지 마라.
            """.formatted(userPrompt, itemsJson, topK);

        Map<String, Object> req = Map.of(
                "model", model,
                "temperature", 0.1,
                "max_tokens", 400,
                "messages", List.of(
                        Map.of("role", "system", "content", system),
                        Map.of("role", "user", "content", user)
                )
        );

        try {
            String content = openAi.post()
                    .uri("/chat/completions")
                    .bodyValue(req)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(java.time.Duration.ofMillis(timeoutMs))
                    .map(m -> ((List<Map<String, Object>>) m.get("choices")).get(0))
                    .map(choice -> (Map<String, Object>) choice.get("message"))
                    .map(msg -> (String) msg.get("content"))
                    .block();

            return parseIds(content);
        } catch (Exception e) {
            throw new RuntimeException("OpenAI(rerank) 호출 실패: " + e.getMessage());
        }
    }

    private List<Long> parseIds(String content) {
        try {
            JsonNode root = mapper.readTree(content);
            JsonNode items = root.path("items");
            if (!items.isArray()) return List.of();
            List<Long> ids = new ArrayList<>();
            for (JsonNode n : items) {
                if (n.has("id")) ids.add(n.get("id").asLong());
            }
            return ids;
        } catch (Exception e) {
            throw new RuntimeException("LLM 응답 파싱 실패: " + e.getMessage());
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
