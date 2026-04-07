package com.farmmarket.controller;

import com.farmmarket.dto.response.ApiResponse;
import com.farmmarket.security.UserPrincipal;
import com.farmmarket.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * GET /api/notifications
     * Get all notifications for the current user
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationService.NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.getUserNotifications(principal.getId())));
    }

    /**
     * GET /api/notifications/unread-count
     * Get unread notification count (for badge display)
     */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.getUnreadCount(principal.getId())));
    }

    /**
     * PATCH /api/notifications/{id}/read
     * Mark a specific notification as read
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationService.NotificationResponse>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.markAsRead(id, principal.getId())));
    }

    /**
     * PATCH /api/notifications/read-all
     * Mark all notifications as read
     */
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAllAsRead(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }

    /**
     * DELETE /api/notifications/{id}
     * Delete a specific notification
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        notificationService.deleteNotification(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Notification deleted", null));
    }
}
