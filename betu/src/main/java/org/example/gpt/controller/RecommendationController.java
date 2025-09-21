package org.example.gpt.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.challenge.entity.Challenge;
import org.example.challenge.entity.ChallengeTag;
import org.example.gpt.service.RecommendationService;
import org.example.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/challenges")
@RequiredArgsConstructor
public class RecommendationController {

    private final UserService userService;
    private final RecommendationService recommendationService;

    /**
     * 예) GET /challenges/recommend?tag=ALGORITHM&prompt=코테+30분+루틴
     * 반환: id, title만
     */
    @GetMapping("/recommend")
    public ResponseEntity<List<ChallengeSummary>> recommendTop3(
            HttpServletRequest request,
            @RequestParam ChallengeTag tag,
            @RequestParam String prompt
    ) {
        Long userId = userService.getUserIdFromToken(request);
        List<Challenge> list = recommendationService.recommendTop3ByTagAndPrompt(tag, prompt);
        var out = list.stream()
                .map(c -> new ChallengeSummary(c.getChallengeId(), c.getChallengeName()))
                .toList();
        return ResponseEntity.ok(out);
    }

    /**
     * 예) GET /challenges/recommend/ids?tag=ALGORITHM&prompt=...
     * 반환: Top-3 id만
     */
    @GetMapping("/recommend/ids")
    public ResponseEntity<List<Long>> recommendTop3Ids(
            HttpServletRequest request,
            @RequestParam ChallengeTag tag,
            @RequestParam String prompt
    ) {
        Long userId = userService.getUserIdFromToken(request);
        return ResponseEntity.ok(recommendationService.recommendTop3Ids(tag, prompt));
    }

    public record ChallengeSummary(Long id, String title) {}
}
