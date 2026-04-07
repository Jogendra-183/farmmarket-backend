package com.farmmarket.controller;

import com.farmmarket.dto.response.ApiResponse;
import com.farmmarket.security.UserPrincipal;
import com.farmmarket.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    /**
     * GET /api/community/posts?page=0&size=10
     * Get all community posts (Community.jsx page - public)
     */
    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<Page<CommunityService.CommunityPostResponse>>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(communityService.getAllPosts(page, size)));
    }

    /**
     * GET /api/community/posts?category=FARMING_TIPS
     * Filter posts by category
     */
    @GetMapping("/posts/category/{category}")
    public ResponseEntity<ApiResponse<Page<CommunityService.CommunityPostResponse>>> getPostsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                communityService.getPostsByCategory(category, page, size)));
    }

    /**
     * POST /api/community/posts
     * Create a new community post (authenticated users only)
     * Body: { title, content, category }
     */
    @PostMapping("/posts")
    public ResponseEntity<ApiResponse<CommunityService.CommunityPostResponse>> createPost(
            @RequestBody CommunityService.CreatePostRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        CommunityService.CommunityPostResponse post = communityService.createPost(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Post created", post));
    }

    /**
     * POST /api/community/posts/{id}/like
     * Like a post
     */
    @PostMapping("/posts/{id}/like")
    public ResponseEntity<ApiResponse<CommunityService.CommunityPostResponse>> likePost(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(communityService.likePost(id)));
    }

    /**
     * DELETE /api/community/posts/{id}
     * Delete a post (owner or admin)
     */
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        boolean isAdmin = principal.getRole().name().equals("ADMIN");
        communityService.deletePost(id, principal.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Post deleted", null));
    }

    /**
     * PATCH /api/community/posts/{id}/pin
     * Admin: Pin/unpin a post
     */
    @PatchMapping("/posts/{id}/pin")
    public ResponseEntity<ApiResponse<CommunityService.CommunityPostResponse>> pinPost(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(communityService.pinPost(id)));
    }
}
