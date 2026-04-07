package com.farmmarket.controller;

import com.farmmarket.dto.request.SubscriptionUpgradeRequest;
import com.farmmarket.dto.response.ApiResponse;
import com.farmmarket.security.UserPrincipal;
import com.farmmarket.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    // ============================
    // BUYER endpoints
    // ============================

    /**
     * GET /api/buyer/subscription
     * Get current buyer's subscription details
     */
    @GetMapping("/api/buyer/subscription")
    public ResponseEntity<ApiResponse<SubscriptionService.SubscriptionResponse>> getMySubscription(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                subscriptionService.getSubscription(principal.getId())));
    }

    /**
     * POST /api/buyer/subscription/upgrade
     * Submit subscription upgrade request (Step 1 & 2 of the frontend dialog)
     * Body: { planType, fullName, email, phone, address, city, state, zipCode,
     *         cardLastFour, billingAddress }
     */
    @PostMapping("/api/buyer/subscription/upgrade")
    public ResponseEntity<ApiResponse<SubscriptionService.SubscriptionResponse>> requestUpgrade(
            @Valid @RequestBody SubscriptionUpgradeRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Upgrade request submitted. Awaiting admin approval.",
                subscriptionService.requestUpgrade(principal.getId(), request)));
    }

    /**
     * POST /api/buyer/subscription/cancel
     * Cancel the current subscription
     */
    @PostMapping("/api/buyer/subscription/cancel")
    public ResponseEntity<ApiResponse<SubscriptionService.SubscriptionResponse>> cancelSubscription(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Subscription cancelled successfully",
                subscriptionService.cancelSubscription(principal.getId())));
    }

    // ============================
    // ADMIN endpoints
    // ============================

    /**
     * GET /api/admin/subscriptions
     * Get all subscriptions
     */
    @GetMapping("/api/admin/subscriptions")
    public ResponseEntity<ApiResponse<List<SubscriptionService.SubscriptionResponse>>> getAllSubscriptions() {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getAllSubscriptions()));
    }

    /**
     * GET /api/admin/subscriptions/pending
     * Get pending upgrade requests (Admin content page)
     */
    @GetMapping("/api/admin/subscriptions/pending")
    public ResponseEntity<ApiResponse<List<SubscriptionService.SubscriptionResponse>>> getPendingRequests() {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getPendingUpgradeRequests()));
    }

    /**
     * POST /api/admin/subscriptions/{id}/approve
     * Approve a subscription upgrade request (Step 3 -> 4 in frontend)
     */
    @PostMapping("/api/admin/subscriptions/{id}/approve")
    public ResponseEntity<ApiResponse<SubscriptionService.SubscriptionResponse>> approveUpgrade(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Upgrade approved successfully",
                subscriptionService.approveUpgrade(id)));
    }
}
