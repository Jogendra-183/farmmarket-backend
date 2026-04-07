package com.farmmarket.controller;

import com.farmmarket.dto.request.OrderRequest;
import com.farmmarket.dto.response.ApiResponse;
import com.farmmarket.dto.response.OrderResponse;
import com.farmmarket.entity.Order;
import com.farmmarket.security.UserPrincipal;
import com.farmmarket.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ============================
    // BUYER endpoints
    // ============================

    /**
     * POST /api/buyer/orders
     * Place a new order from the cart
     * Body: { items: [{productId, quantity}], shippingAddress, shippingCity, shippingState, shippingZip }
     */
    @PostMapping("/api/buyer/orders")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @Valid @RequestBody OrderRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        OrderResponse order = orderService.placeOrder(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully", order));
    }

    /**
     * GET /api/buyer/orders
     * Get all orders for the logged-in buyer
     */
    @GetMapping("/api/buyer/orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getBuyerOrders(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.getBuyerOrders(principal.getId())));
    }

    /**
     * GET /api/buyer/orders/{id}
     * Get a specific order by ID
     */
    @GetMapping("/api/buyer/orders/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.getOrderById(id, principal.getId())));
    }

    // ============================
    // FARMER endpoints
    // ============================

    /**
     * GET /api/farmer/orders
     * Get all orders containing the farmer's products
     */
    @GetMapping("/api/farmer/orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getFarmerOrders(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.getFarmerOrders(principal.getId())));
    }

    /**
     * GET /api/farmer/orders?status=PROCESSING
     * Get farmer's orders filtered by status
     */
    @GetMapping("/api/farmer/orders/by-status")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getFarmerOrdersByStatus(
            @RequestParam Order.OrderStatus status,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.getFarmerOrdersByStatus(principal.getId(), status)));
    }

    /**
     * PATCH /api/farmer/orders/{id}/status
     * Update order status (e.g., PROCESSING -> SHIPPED -> DELIVERED)
     * Body: { status: "SHIPPED" }
     */
    @PatchMapping("/api/farmer/orders/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam Order.OrderStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Order status updated",
                orderService.updateOrderStatus(id, status)));
    }

    // ============================
    // ADMIN endpoints
    // ============================

    /**
     * GET /api/admin/orders
     * Get all orders in the system
     */
    @GetMapping("/api/admin/orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {
        return ResponseEntity.ok(ApiResponse.success(orderService.getAllOrders()));
    }

    /**
     * PATCH /api/admin/orders/{id}/status
     * Admin override order status
     */
    @PatchMapping("/api/admin/orders/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> adminUpdateOrderStatus(
            @PathVariable Long id,
            @RequestParam Order.OrderStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Order status updated",
                orderService.updateOrderStatus(id, status)));
    }
}
