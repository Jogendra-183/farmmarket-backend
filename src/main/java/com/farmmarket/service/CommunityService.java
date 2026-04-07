package com.farmmarket.service;

import com.farmmarket.entity.CommunityPost;
import com.farmmarket.entity.User;
import com.farmmarket.exception.ResourceNotFoundException;
import com.farmmarket.exception.UnauthorizedException;
import com.farmmarket.repository.CommunityPostRepository;
import com.farmmarket.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityPostRepository communityPostRepository;
    private final UserRepository userRepository;

    public Page<CommunityPostResponse> getAllPosts(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return communityPostRepository.findAllByOrderByIsPinnedDescCreatedAtDesc(pageable)
                .map(this::mapToResponse);
    }

    public Page<CommunityPostResponse> getPostsByCategory(String category, int page, int size) {
        CommunityPost.PostCategory cat = CommunityPost.PostCategory.valueOf(category.toUpperCase());
        return communityPostRepository.findByCategory(cat, PageRequest.of(page, size))
                .map(this::mapToResponse);
    }

    @Transactional
    public CommunityPostResponse createPost(Long authorId, CreatePostRequest request) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", authorId));

        CommunityPost post = CommunityPost.builder()
                .author(author)
                .title(request.getTitle())
                .content(request.getContent())
                .category(request.getCategory())
                .build();

        return mapToResponse(communityPostRepository.save(post));
    }

    @Transactional
    public CommunityPostResponse likePost(Long postId) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        post.setLikeCount(post.getLikeCount() + 1);
        return mapToResponse(communityPostRepository.save(post));
    }

    @Transactional
    public void deletePost(Long postId, Long userId, boolean isAdmin) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        if (!isAdmin && !post.getAuthor().getId().equals(userId)) {
            throw new UnauthorizedException("You can only delete your own posts");
        }

        communityPostRepository.delete(post);
    }

    @Transactional
    public CommunityPostResponse pinPost(Long postId) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        post.setIsPinned(!post.getIsPinned());
        return mapToResponse(communityPostRepository.save(post));
    }

    private CommunityPostResponse mapToResponse(CommunityPost post) {
        return CommunityPostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory())
                .authorId(post.getAuthor().getId())
                .authorName(post.getAuthor().getName())
                .authorRole(post.getAuthor().getRole())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .isPinned(post.getIsPinned())
                .createdAt(post.getCreatedAt())
                .build();
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CommunityPostResponse {
        private Long id;
        private String title;
        private String content;
        private CommunityPost.PostCategory category;
        private Long authorId;
        private String authorName;
        private User.Role authorRole;
        private Integer likeCount;
        private Integer commentCount;
        private Boolean isPinned;
        private LocalDateTime createdAt;
    }

    @Data
    public static class CreatePostRequest {
        private String title;
        private String content;
        private CommunityPost.PostCategory category;
    }
}
