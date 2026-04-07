package com.farmmarket.controller;

import com.farmmarket.dto.response.ApiResponse;
import com.farmmarket.security.UserPrincipal;
import com.farmmarket.service.AnalyticsService;
import com.farmmarket.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AnalyticsService analyticsService;

    // ============================
    // Shared Profile endpoints (all roles)
    // ============================

    /**
     * GET /api/users/profile
     * Get the current user's full profile (Profile.jsx page)
     */
    @GetMapping("/api/users/profile")
    public ResponseEntity<ApiResponse<UserService.UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(userService.getProfile(principal.getId())));
    }

    /**
     * PUT /api/users/profile
     * Update profile info (Account.jsx page - name, phone, address, farm info)
     */
    @PutMapping("/api/users/profile")
    public ResponseEntity<ApiResponse<UserService.UserProfileResponse>> updateProfile(
            @RequestBody UserService.UpdateProfileRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Profile updated",
                userService.updateProfile(principal.getId(), request)));
    }

    /**
     * POST /api/users/change-password
     * Change password (Account.jsx page)
     * Body: { currentPassword, newPassword }
     */
    @PostMapping("/api/users/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestBody UserService.ChangePasswordRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        userService.changePassword(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    // ============================
    // Farmer Dashboard
    // ============================

    /**
     * GET /api/farmer/dashboard
     * Farmer dashboard stats (Dashboard.jsx page)
     */
    @GetMapping("/api/farmer/dashboard")
    public ResponseEntity<ApiResponse<AnalyticsService.FarmerAnalyticsResponse>> getFarmerDashboard(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                analyticsService.getFarmerAnalytics(principal.getId())));
    }

    /**
     * GET /api/farmer/analytics
     * Detailed farmer analytics (Analytics.jsx page)
     */
    @GetMapping("/api/farmer/analytics")
    public ResponseEntity<ApiResponse<AnalyticsService.FarmerAnalyticsResponse>> getFarmerAnalytics(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                analyticsService.getFarmerAnalytics(principal.getId())));
    }

    // ============================
    // Buyer Dashboard
    // ============================

    /**
     * GET /api/buyer/dashboard
     * Buyer dashboard stats
     */
    @GetMapping("/api/buyer/dashboard")
    public ResponseEntity<ApiResponse<AnalyticsService.BuyerDashboardResponse>> getBuyerDashboard(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                analyticsService.getBuyerDashboard(principal.getId())));
    }

    // ============================
    // Admin User Management
    // ============================

    /**
     * GET /api/admin/users?page=0&size=20
     * Admin: Get all users (Users.jsx page)
     */
    @GetMapping("/api/admin/users")
    public ResponseEntity<ApiResponse<?>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers(page, size)));
    }

    /**
     * PATCH /api/admin/users/{id}/toggle-status
     * Admin: Activate/deactivate a user
     */
    @PatchMapping("/api/admin/users/{id}/toggle-status")
    public ResponseEntity<ApiResponse<UserService.UserProfileResponse>> toggleUserStatus(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.toggleUserStatus(id)));
    }

    /**
     * DELETE /api/admin/users/{id}
     * Admin: Delete a user
     */
    @DeleteMapping("/api/admin/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted", null));
    }

    /**
     * GET /api/admin/dashboard
     * Admin dashboard analytics (Dashboard.jsx for admin)
     */
    @GetMapping("/api/admin/dashboard")
    public ResponseEntity<ApiResponse<AnalyticsService.AdminAnalyticsResponse>> getAdminDashboard() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getAdminAnalytics()));
    }

    /**
     * GET /api/admin/analytics
     * Admin analytics page
     */
    @GetMapping("/api/admin/analytics")
    public ResponseEntity<ApiResponse<AnalyticsService.AdminAnalyticsResponse>> getAdminAnalytics() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getAdminAnalytics()));
    }
}
