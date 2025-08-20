package org.example.community;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.community.dto.*;
import org.example.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/community")
@Slf4j
public class CommunityController {

    private final CommunityService communityService;
    private final UserService userService; // getUserIdFromToken(HttpServletRequest)

    // 게시글 작성 (이미지 포함: multipart/form-data)
    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "게시글 작성")
    public ResponseEntity<Long> createPost(
            HttpServletRequest request,
            @Valid @ModelAttribute PostCreateRequest req
    ) throws IOException {
        Long userId = userService.getUserIdFromToken(request);
        log.info(">>> createPost called by userId: {}", userId);
        log.info(">>> PostCreateRequest: {}, {}", req.getCrewId(), req.getContent());
        return ResponseEntity.ok(communityService.createPost(userId, req));
    }

    // 게시글 수정 (본문만 — 서비스에 이미지 수정은 추후 예정이라고 주석 있음)
    @PatchMapping("/posts/{postId}")
    @Operation(summary = "게시글 수정")
    public ResponseEntity<Void> updatePost(
            HttpServletRequest request,
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest req
    ) {
        Long userId = userService.getUserIdFromToken(request);
        communityService.updatePost(userId, postId, req);
        return ResponseEntity.ok().build();
    }

    // 댓글 작성
    @PostMapping("/comments")
    @Operation(summary = "댓글 작성")
    public ResponseEntity<Long> addComment(
            HttpServletRequest request,
            @Valid @RequestBody CommentCreateRequest req
    ) {
        Long userId = userService.getUserIdFromToken(request);
        return ResponseEntity.ok(communityService.addComment(userId, req));
    }

    // 댓글 삭제
    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "댓글 삭제")
    public ResponseEntity<Void> deleteComment(
            HttpServletRequest request,
            @PathVariable Long commentId
    ) {
        Long userId = userService.getUserIdFromToken(request);
        communityService.deleteComment(userId, commentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/posts/{postId}")
    @Operation(summary = "게시글 삭제")
    public ResponseEntity<Void> deletePost(
            HttpServletRequest request,
            @PathVariable Long postId
    ) {
        Long userId = userService.getUserIdFromToken(request);
        communityService.deletePost(userId, postId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/crews/{crewId}/posts")
    @Operation(summary = "크루별 게시글 목록 (페이지네이션)")
    public ResponseEntity<Page<PostSummaryResponse>> getPostsByCrew(
            @PathVariable Long crewId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(communityService.getPostsByCrew(crewId, pageable));
    }

    @GetMapping("/posts/{postId}")
    @Operation(summary = "게시글 상세 (이미지 + 댓글)")
    public ResponseEntity<PostDetailResponse> getPostDetail(
            @PathVariable Long postId
    ) {
        return ResponseEntity.ok(communityService.getPostDetail(postId));
    }

    @PostMapping("/posts/{postId}/like")
    @Operation(summary = "게시글 좋아요 토글")
    public ResponseEntity<PostLikeResponse> togglePostLike(
            HttpServletRequest request,
            @PathVariable Long postId
    ) {
        Long userId = userService.getUserIdFromToken(request);
        return ResponseEntity.ok(communityService.togglePostLike(userId, postId));
    }
}
