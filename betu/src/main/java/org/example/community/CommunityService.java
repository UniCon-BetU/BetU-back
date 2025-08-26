package org.example.community;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.community.dto.*;
import org.example.community.entity.Comment;
import org.example.community.entity.Post;
import org.example.community.entity.PostImage;
import org.example.community.entity.PostLike;
import org.example.community.repository.CommentRepository;
import org.example.community.repository.PostImageRepository;
import org.example.community.repository.PostLikeRepository;
import org.example.community.repository.PostRepository;
import org.example.crew.entity.Crew;
import org.example.crew.repository.CrewRepository;
import org.example.user.User;
import org.example.user.UserRepository;
import org.example.general.S3Uploader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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

    // 게시글 상세 (이미지 + 댓글)
    @Transactional(readOnly = true)
    public PostDetailResponse getPostDetail(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글이 없습니다."));

        List<PostImage> images = postImageRepository.findByPost_PostIdOrderBySortOrderAsc(postId);
        List<String> imageUrls = images.stream().map(PostImage::getImageUrl).toList();

        List<Comment> comments = commentRepository.findByPost_PostId(postId);
        List<PostDetailResponse.CommentItem> commentItems = comments.stream()
                .map(c -> new PostDetailResponse.CommentItem(
                        c.getCommentId(),
                        c.getUser().getUserId(),
                        c.getUser().getUserName(), // User에 맞게 수정
                        c.getCommentContent()
                ))
                .toList();

        boolean liked = postLikeRepository.existsByUserIdAndPostId(userId, postId);

        return new PostDetailResponse(
                post.getPostId(),
                post.getCrew() != null ? post.getCrew().getCrewId() : null,
                post.getUser().getUserId(),
                post.getUser().getUserName(), // 필요 시 변경
                post.getPostTitle(),
                post.getPostContent(),
                post.getPostLikeCnt(),
                imageUrls,
                commentItems,
                liked
        );
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

    // 댓글 작성
    @Transactional
    public Long addComment(Long userId, CommentCreateRequest req) {
        Post post = postRepository.findById(req.getPostId())
                .orElseThrow(() -> new EntityNotFoundException("게시글 없음"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자 없음"));

        Comment c = Comment.builder()
                .post(post)
                .user(user)
                .commentContent(req.getContent())
                .build();
        return commentRepository.save(c).getCommentId();
    }

//    @Transactional
//    public Long addReply(Long userId, Long parentCommentId, String content) {
//        Comment parent = commentRepository.findById(parentCommentId)
//                .orElseThrow(() -> new EntityNotFoundException("부모 댓글 없음"));
//
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new EntityNotFoundException("사용자 없음"));
//
//        Comment reply = Comment.builder()
//                .post(parent.getPost()) // 같은 게시글에 속해야 함
//                .user(user)
//                .parent(parent)         // ✅ 부모 설정
//                .commentContent(content)
//                .build();
//
//        // 양방향 편의
//        parent.addChild(reply);
//
//        return commentRepository.save(reply).getCommentId();
//    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글 없음"));
        if (!c.getUser().getUserId().equals(userId)) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }
        commentRepository.deleteById(commentId);
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