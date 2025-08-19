package org.example.challenge;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.challenge.dto.*;
import org.example.challenge.entity.ChallengeTag;
import org.example.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/challenges")
public class ChallengeController {
    private final UserService userService;
    private final ChallengeService challengeService;

    @PostMapping
    @Operation(summary = "챌린지 생성")
    public ResponseEntity<ChallengeDetailResponse> createChallenge(
            HttpServletRequest request,
            @Valid @RequestBody ChallengeCreateRequest dto
    ) {
        Long userId = userService.getUserIdFromToken(request);
        return ResponseEntity.ok(challengeService.create(userId, dto));
    }

    @GetMapping("/{challengeId}")
    @Operation(summary = "챌린지 상세 조회")
    public ResponseEntity<ChallengeDetailResponse> getChallenge(
            HttpServletRequest request,
            @PathVariable Long challengeId
    ) {
        Long userId = userService.getUserIdFromToken(request);
        return ResponseEntity.ok(challengeService.getChallengeWithDetails(userId, challengeId));
    }

    @PostMapping("/{challengeId}/join")
    @Operation(summary = "챌린지 참가")
    public ResponseEntity<ChallengeDetailResponse> joinChallenge(
            HttpServletRequest request,
            @PathVariable Long challengeId
    ) {
        Long userId = userService.getUserIdFromToken(request);
        return ResponseEntity.ok(challengeService.joinChallenge(userId, challengeId));
    }

    @DeleteMapping("/{challengeId}")
    @Operation(summary = "챌린지 삭제")
    public ResponseEntity<Void> deleteChallenge(
            HttpServletRequest request,
            @PathVariable Long challengeId
    ) {
        Long userId = userService.getUserIdFromToken(request);
        challengeService.deleteChallenge(userId, challengeId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{challengeId}/like")
    @Operation(summary = "챌린지 좋아요 토글")
    public ResponseEntity<ChallengeLikeResponse> likeChallenge(
            HttpServletRequest request,
            @PathVariable Long challengeId
    ) {
        Long userId = userService.getUserIdFromToken(request);
        return ResponseEntity.ok(
                challengeService.challengeLike(userId, challengeId)
        );
    }

    @GetMapping("/popular")
    @Operation(summary = "인기 챌린지 TOP3 조회")
    public ResponseEntity<List<ChallengeResponse>> getPopularChallenges() {
        return ResponseEntity.ok(challengeService.getPopularChallenges());
    }

    @GetMapping("/me/likes")
    @Operation(summary = "내가 좋아요한 챌린지 조회")
    public ResponseEntity<List<ChallengeResponse>> getLikedChallenges(HttpServletRequest request) {
        Long userId = userService.getUserIdFromToken(request);
        return ResponseEntity.ok(challengeService.getLikedChallenges(userId));
    }

    @GetMapping("/me")
    @Operation(summary = "내가 참여한 챌린지 조회")
    public ResponseEntity<List<ChallengeResponse>> getMyChallenges(HttpServletRequest request) {
        Long userId = userService.getUserIdFromToken(request);
        return ResponseEntity.ok(challengeService.getMyChallenges(userId));
    }

    @GetMapping
    @Operation(summary = "전체 챌린지 조회")
    public ResponseEntity<List<ChallengeResponse>> getAllChallenges() {
        return ResponseEntity.ok(challengeService.getAllChallenges());
    }

    @GetMapping("/search")
    @Operation(summary = "챌린지 검색 (이름/설명 키워드)")
    public ResponseEntity<List<ChallengeResponse>> searchChallenges(
            @RequestParam("kw") String keyword
    ) {
        return ResponseEntity.ok(challengeService.searchChallenges(keyword));
    }

    @GetMapping("/by-tags")
    @Operation(summary = "태그로 챌린지 조회 (하나라도 포함: OR)")
    public ResponseEntity<List<ChallengeResponse>> getChallengesByTags(
            @RequestParam("tags") Set<ChallengeTag> tags
    ) {
        return ResponseEntity.ok(challengeService.getChallengesByTags(tags));
    }

}
