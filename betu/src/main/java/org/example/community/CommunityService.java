package org.example.community;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.community.dto.*;
import org.example.community.entity.*;
import org.example.community.repository.*;
import org.example.crew.entity.Crew;
import org.example.crew.entity.UserCrew;
import org.example.crew.entity.UserCrewRole;
import org.example.crew.repository.CrewRepository;
import org.example.crew.repository.UserCrewRepository;
import org.example.user.User;
import org.example.user.UserRepository;
import org.example.general.S3Uploader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final CommentRepository commentRepository;
    private final CrewRepository crewRepository;
    private final UserRepository userRepository;
    private final S3Uploader s3Uploader;
    private final PostLikeRepository postLikeRepository;
    private final PostReportRepository postReportRepository;
    private final UserCrewRepository userCrewRepository;
    private final CommentLikeRepository commentLikeRepository;

    // 게시글 작성
    @Transactional
    public Long createPost(Long userId, PostCreateRequest req, List<MultipartFile> images) throws IOException {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자 없음"));
        Crew crew = null;
        if (req.getCrewId() != null) {
            crew = crewRepository.findById(req.getCrewId())
                    .orElseThrow(() -> new EntityNotFoundException("그룹 없음"));
        }

        Post post = new Post(null, crew, author, req.getTitle(), req.getContent(), 0);
        Post saved = postRepository.save(post);

        // 이미지 업로드
        if (images != null && !images.isEmpty()) {
            int order = 0;
            for (MultipartFile file : images) {
                Long crewIdValue = (crew != null) ? crew.getCrewId() : 0L;
                String prefix = String.format("crew/%d/post/%d", crewIdValue, saved.getPostId());
                String url = s3Uploader.uploadImageUnderPrefix(file, prefix);

                PostImage pi = PostImage.builder()
                        .post(saved)
                        .imageUrl(url)
                        .sortOrder(order)
                        .isThumbnail(order == 0)
                        .build();
                postImageRepository.save(pi);
                order++;
            }
        }
        return saved.getPostId();
    }

    // 게시글 수정 -> 추후 수정 예정 (이미지까지)
    @Transactional
    public void updatePost(Long userId, Long postId, PostUpdateRequest req) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글 없음"));
        if (!post.getUser().getUserId().equals(userId)) {
            throw new SecurityException("수정 권한이 없습니다.");
        }

        Post updated = new Post(post.getPostId(), post.getCrew(), post.getUser(), req.getTitle(), req.getContent(), 0);
        postRepository.save(updated);
    }

    // 게시글 삭제
    public void deletePost(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글이 없습니다."));
        if (!post.getUser().getUserId().equals(userId)) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }

        postImageRepository.deleteByPost_PostId(postId);
        commentRepository.deleteByPost_PostId(postId);
        postLikeRepository.deleteByUserIdAndPostId(userId, postId); // 내 좋아요만 삭제 X
        postLikeRepository.deleteByPostId(postId);
        postRepository.deleteById(postId);
    }

    // 전체 게시글 조회
    @Transactional(readOnly = true)
    public List<PostSummaryResponse> getAllPosts() {
        List<Post> posts = postRepository.findAllByOrderByPostIdDesc();

        return toPostSummaryResponses(posts);
    }

    // 크루별 게시글 목록
    @Transactional(readOnly = true)
    public List<PostSummaryResponse> getPostsByCrew(Long crewId) {
        List<Post> posts;

        if (crewId == null) {
            // crewId 없으면 crew == null인 게시글 전체 조회
            posts = postRepository.findByCrewIsNullOrderByPostIdDesc();
        } else {
            // crewId 있으면 해당 crew 게시글 전체 조회
            posts = postRepository.findByCrew_CrewIdOrderByPostIdDesc(crewId);
        }

        return toPostSummaryResponses(posts);
    }

    @Transactional(readOnly = true)
    public List<PostSummaryResponse> getPopularPosts(Long crewId) {
        List<Post> posts = (crewId == null)
                ? postRepository.findByCrewIsNullAndPostLikeCntGreaterThanEqualOrderByPostIdDesc(10)
                : postRepository.findByCrew_CrewIdAndPostLikeCntGreaterThanEqualOrderByPostIdDesc(crewId, 10);
        return toPostSummaryResponses(posts);
    }

    @Transactional(readOnly = true)
    public List<PostSummaryResponse> searchPostsLatest(String query, Long crewId) {
        if (query == null || query.isBlank()) return List.of();
        return toPostSummaryResponses(
                postRepository.searchLatestByKeyword(crewId, query.trim())
        );
    }

    @Transactional(readOnly = true)
    public PostDetailResponse getPostDetail(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글이 없습니다."));

        // 이미지
        List<PostImage> images = postImageRepository.findByPost_PostIdOrderBySortOrderAsc(postId);
        List<String> imageUrls = images.stream().map(PostImage::getImageUrl).toList();

        // 댓글 전체 로드 (user, parent 포함 - @EntityGraph로 N+1 회피)
        List<Comment> all = commentRepository.findByPost_PostIdOrderByCommentIdAsc(postId);

        // 내가 좋아요 누른 댓글 id를 한 번에 조회
        Set<Long> likedSet = Collections.emptySet();
        if (userId != null && !all.isEmpty()) {
            List<Long> ids = all.stream().map(Comment::getCommentId).toList();
            likedSet = new HashSet<>(commentLikeRepository.findLikedCommentIdsByUser(userId, ids));
        }

        // 트리 변환(좋아요 카운트/likedByMe 반영)
        List<CommentTreeResponse> commentTree = buildCommentTree(all, likedSet);

        boolean liked = postLikeRepository.existsByUserIdAndPostId(userId, postId);

        return new PostDetailResponse(
                post.getPostId(),
                post.getCrew() != null ? post.getCrew().getCrewId() : null,
                post.getUser().getUserId(),
                post.getUser().getUserName(),
                post.getPostTitle(),
                post.getPostContent(),
                post.getPostLikeCnt(),
                imageUrls,
                commentTree,
                liked
        );
    }

    private List<CommentTreeResponse> buildCommentTree(List<Comment> all, Set<Long> likedSet) {
        // 1) commentId -> DTO 미리 생성 (삭제/likedByMe 반영)
        Map<Long, CommentTreeResponse> dtoMap = new LinkedHashMap<>();
        for (Comment c : all) {
            boolean likedByMe = likedSet != null && likedSet.contains(c.getCommentId());
            dtoMap.put(c.getCommentId(), CommentTreeResponse.of(c, likedByMe));
        }

        // 2) 루트 모으기 + 부모-자식 연결
        List<CommentTreeResponse> roots = new ArrayList<>();
        for (Comment c : all) {
            CommentTreeResponse dto = dtoMap.get(c.getCommentId());
            if (c.getParent() == null) {
                // 루트 댓글
                roots.add(dto);
            } else {
                // 부모가 있으면 부모 DTO의 replies에 추가
                Comment parent = c.getParent();
                CommentTreeResponse parentDto = dtoMap.get(parent.getCommentId());
                if (parentDto != null) {
                    parentDto.getReplies().add(dto);      // 또는 parentDto.addReply(dto);
                } else {
                    // 데이터 무결성 방어: 부모 DTO가 없으면 루트로라도 노출
                    roots.add(dto);
                }
            }
        }
        return roots;
    }

    // 게시글 좋아요
    @Transactional
    public PostLikeResponse togglePostLike(Long userId, Long postId) {
        var post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글이 없습니다."));
        boolean exists = postLikeRepository.existsByUserIdAndPostId(userId, postId);

        if (exists) {
            postLikeRepository.deleteByUserIdAndPostId(userId, postId);
            post.decreaseLike();
        } else {
            var user = userRepository.getReferenceById(userId);
            postLikeRepository.save(new PostLike(userId, postId));
            post.increaseLike();
        }

        return new PostLikeResponse(!exists, post.getPostLikeCnt());
    }


    @Transactional
    public Long createRootComment(Long userId, CommentCreateRequest commentCreateRequest) {
        if (commentCreateRequest.getContent() == null || commentCreateRequest.getContent().isBlank()) {
            throw new IllegalArgumentException("댓글 내용이 비어 있습니다.");
        }

        Post post = postRepository.findById(commentCreateRequest.getPostId())
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .commentContent(commentCreateRequest.getContent())
                .build();

        return commentRepository.save(comment).getCommentId();
    }

    @Transactional
    public Long createReply(Long userId, ReplyCommentCreateRequest replyCommentCreateRequest) {
        if (replyCommentCreateRequest.getCommentContent() == null || replyCommentCreateRequest.getCommentContent().isBlank()) {
            throw new IllegalArgumentException("댓글 내용이 비어 있습니다.");
        }

        Comment parent = commentRepository.findById(replyCommentCreateRequest.getCommentId())
                .orElseThrow(() -> new EntityNotFoundException("부모 댓글을 찾을 수 없습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // 부모와 동일한 게시글에 속하도록 보장
        Comment reply = Comment.builder()
                .post(parent.getPost())
                .user(user)
                .parent(parent)
                .commentContent(replyCommentCreateRequest.getCommentContent())
                .build();

        // 양방향 편의 메서드 (children 컬렉션에도 추가)
        parent.addChild(reply);

        return commentRepository.save(reply).getCommentId();
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글 없음"));

        if (!comment.getUser().getUserId().equals(userId)) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }

        comment.softDelete();
    }

    @Transactional
    public Map<String, Object> toggleCommentLike(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글 없음"));
        User user = userRepository.getReferenceById(userId);

        boolean exists = commentLikeRepository.existsByUser_UserIdAndComment_CommentId(userId, commentId);

        if (exists) {
            commentLikeRepository.deleteByUser_UserIdAndComment_CommentId(userId, commentId);
            comment.decreaseLike();
        } else {
            commentLikeRepository.save(new CommentLike(comment, user));
            comment.increaseLike();
        }

        Map<String, Object> res = new HashMap<>();
        res.put("liked", !exists);
        res.put("likeCount", comment.getLikeCount());
        return res;
    }

    /** 유저가 게시글 신고 */
    public Long reportPost(Long reporterId, Long postId, PostReportCreateRequest req) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new EntityNotFoundException("사용자 없음"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글 없음"));

        if (postReportRepository.existsByReporter_UserIdAndPost_PostId(reporterId, postId)) {
            throw new IllegalStateException("이미 신고한 게시글입니다.");
        }

        String reason = (req == null || req.getReason() == null) ? "" : req.getReason().trim();

        PostReport pr = PostReport.builder()
                .post(post)
                .reporter(reporter)
                .reason(reason)
                .status(ReportStatus.PENDING)
                .build();

        postReportRepository.save(pr);
        return pr.getReportId();
    }

    /** 내가 처리 가능한 PENDING 신고 목록 */
    @Transactional(readOnly = true)
    public List<PostReportResponse> getMyPendingReports(Long viewerUserId) {
        User me = userRepository.findById(viewerUserId)
                .orElseThrow(() -> new EntityNotFoundException("사용자 없음"));

        List<PostReport> all = postReportRepository.findAllByStatusWithPostAndCrew(ReportStatus.PENDING);

        if (me.isAdmin()) {
            return all.stream().map(PostReportResponse::from).toList();
        }

        // 크루 OWNER인 건만 남기기 (crew=null=public 은 관리자만)
        return all.stream()
                .filter(pr -> canModerate(me, pr))
                .map(PostReportResponse::from)
                .toList();
    }

    /** 신고 수락(인정) */
    public void acceptReport(Long moderatorUserId, Long reportId) {
        User me = userRepository.findById(moderatorUserId)
                .orElseThrow(() -> new EntityNotFoundException("사용자 없음"));

        PostReport pr = postReportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("신고 없음"));

        assertCanModerate(me, pr);
        pr.accept();
        Post post = pr.getPost();

        postImageRepository.deleteByPost_PostId(post.getPostId());
        commentRepository.deleteByPost_PostId(post.getPostId());
        postLikeRepository.deleteByPostId(post.getPostId());

        postRepository.delete(post);
    }

    /** 신고 기각 */
    public void rejectReport(Long moderatorUserId, Long reportId) {
        User me = userRepository.findById(moderatorUserId)
                .orElseThrow(() -> new EntityNotFoundException("사용자 없음"));

        PostReport pr = postReportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("신고 없음"));

        assertCanModerate(me, pr);
        pr.reject();
    }

    // ===== 권한 판별 =====

    private void assertCanModerate(User me, PostReport pr) {
        if (!canModerate(me, pr)) {
            throw new SecurityException("신고 처리 권한이 없습니다.");
        }
    }

    private boolean canModerate(User me, PostReport pr) {
        if (me.isAdmin()) return true;

        // 게시글 crew가 null이면 public => 관리자만 가능 (crew OWNER 불가)
        var crew = pr.getPost().getCrew();
        if (crew == null) {
            return false;
        }
        // crew 챌린지 => 해당 crew OWNER이면 가능
        return userCrewRepository.existsByUser_UserIdAndCrew_CrewIdAndUserCrewRole(
                me.getUserId(), crew.getCrewId(), UserCrewRole.OWNER
        );
    }

    private List<PostSummaryResponse> toPostSummaryResponses(List<Post> posts) {
        return posts.stream()
                .map(p -> {
                    // 썸네일(첫 번째 이미지)
                    List<PostImage> images = postImageRepository
                            .findByPost_PostIdOrderBySortOrderAsc(p.getPostId());
                    String thumbnail = images.isEmpty() ? null : images.get(0).getImageUrl();

                    // 댓글 개수
                    int commentCount = commentRepository.findByPost_PostId(p.getPostId()).size();

                    // 프리뷰(최대 120자)
                    String previewContent = preview(p.getPostContent(), 120);

                    return new PostSummaryResponse(
                            p.getPostId(),
                            p.getCrew() != null ? p.getCrew().getCrewId() : null,
                            p.getUser().getUserId(),
                            p.getUser().getUserName(),
                            p.getPostTitle(),
                            previewContent,
                            p.getPostLikeCnt(),
                            commentCount,
                            thumbnail
                    );
                })
                .toList();
    }

    private String preview(String content, int max) {
        if (content == null) return "";
        return content.length() <= max ? content : content.substring(0, max) + "...";
    }

}