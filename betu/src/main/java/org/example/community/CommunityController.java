package org.example.community;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.community.dto.*;
import org.example.user.UserService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
            @ParameterObject @ModelAttribute PostCreateRequest req,
            @RequestPart(name = "images", required = false) List<MultipartFile> images // 파일 (없으면 null)
    ) throws IOException {
        Long userId = userService.getUserIdFromToken(request);

        log.info(">>> createPost called by userId: {}", userId);
        log.info(">>> PostCreateRequest: crewId={}, title={}, content={}",
                req.getCrewId(), req.getTitle(), req.getContent());
        log.info(">>> images: {}", (images == null ? "null" : images.size()));

        Long postId = communityService.createPost(userId, req, images);
        return ResponseEntity.ok(postId);
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
    @PostMapping("/root")
    @Operation(summary = "루트 댓글 작성", description = "게시글에 새로운 댓글을 작성합니다.")
    public ResponseEntity<Long> createRootComment(
            HttpServletRequest request,
            @RequestBody CommentCreateRequest req
    ) {
        Long userId = userService.getUserIdFromToken(request);
        Long commentId = communityService.createRootComment(userId, req);
        return ResponseEntity.ok(commentId);
    }

    @PostMapping("/reply")
    @Operation(summary = "답글 작성", description = "특정 댓글에 답글을 작성합니다.")
    public ResponseEntity<Long> createReply(
            HttpServletRequest request,
            @RequestBody ReplyCommentCreateRequest req
    ) {
        Long userId = userService.getUserIdFromToken(request);
        Long replyId = communityService.createReply(userId, req);
        return ResponseEntity.ok(replyId);
    }

    @PostMapping("/{commentId}/like")
    public ResponseEntity<Map<String, Object>> toggleCommentLike(
            @PathVariable Long commentId,
            HttpServletRequest request
    ) {
        Long userId = userService.getUserIdFromToken(request);

        Map<String, Object> result = communityService.toggleCommentLike(userId, commentId);
        return ResponseEntity.ok(result);
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

    @GetMapping("/posts")
    @Operation(summary = "전체 게시글 목록")
    public ResponseEntity<List<PostSummaryResponse>> getAllPosts(
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(communityService.getAllPosts());
    }

    @GetMapping("/crews/posts")
    @Operation(summary = "크루별 게시글 조회 (최신순)")
    public ResponseEntity<List<PostSummaryResponse>> getPostsByCrew(
            @RequestParam(required = false) Long crewId
    ) {
        return ResponseEntity.ok(communityService.getPostsByCrew(crewId));
    }

    @GetMapping("/posts/popular")
    @Operation(summary = "크루별 게시글 조회 (인기순)", description = "좋아요 10개 이상 게시물. crewId 없으면 전체(crewId=null)만, 있으면 해당 크루만 조회")
    public ResponseEntity<List<PostSummaryResponse>> getPopularPosts(
            @RequestParam(required = false) Long crewId
    ) {
        return ResponseEntity.ok(communityService.getPopularPosts(crewId));
    }

    @GetMapping("/posts/search")
    @Operation(summary = "게시글 검색(최신순, 페이징 없음)",
            description = "postTitle 또는 postContent에 키워드가 포함된 게시글을 postId DESC로 정렬해 반환")
    public ResponseEntity<List<PostSummaryResponse>> searchPostsLatest(@RequestParam(required = false) Long crewId, @RequestParam String query) {
        return ResponseEntity.ok(communityService.searchPostsLatest(query, crewId));
    }

    @GetMapping("/posts/{postId}")
    @Operation(summary = "게시글 상세 (이미지 + 댓글)")
    public ResponseEntity<PostDetailResponse> getPostDetail(
            HttpServletRequest request,
            @PathVariable Long postId
    ) {
        Long userId = userService.getUserIdFromToken(request);
        return ResponseEntity.ok(communityService.getPostDetail(userId, postId));
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

    @PostMapping("/posts/{postId}")
    @Operation(summary = "게시글 신고", description = "게시글을 신고합니다.")
    public ResponseEntity<Long> reportPost(
            HttpServletRequest request,
            @PathVariable Long postId,
            @RequestBody(required = false) PostReportCreateRequest req
    ) {
        Long userId = userService.getUserIdFromToken(request);
        Long reportId = communityService.reportPost(userId, postId, req);
        return ResponseEntity.ok(reportId);
    }

    @GetMapping("/pending")
    @Operation(summary = "내가 처리 가능한 신고 목록", description = "관리자/크루 OWNER가 처리해야 할 PENDING 상태의 신고 목록을 가져옵니다.")
    public ResponseEntity<List<PostReportResponse>> getMyPendingReports(
            HttpServletRequest request
    ) {
        Long userId = userService.getUserIdFromToken(request);
        List<PostReportResponse> list = communityService.getMyPendingReports(userId);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/{reportId}/accept")
    @Operation(summary = "신고 수락", description = "신고를 수락하고 해당 게시글을 삭제합니다.")
    public ResponseEntity<Void> acceptReport(
            HttpServletRequest request,
            @PathVariable Long reportId
    ) {
        Long userId = userService.getUserIdFromToken(request);
        communityService.acceptReport(userId, reportId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{reportId}/reject")
    @Operation(summary = "신고 기각", description = "신고를 기각합니다.")
    public ResponseEntity<Void> rejectReport(
            HttpServletRequest request,
            @PathVariable Long reportId
    ) {
        Long userId = userService.getUserIdFromToken(request);
        communityService.rejectReport(userId, reportId);
        return ResponseEntity.ok().build();
    }
}
