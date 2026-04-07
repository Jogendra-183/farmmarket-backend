package com.farmmarket.controller;

import com.farmmarket.dto.response.ApiResponse;
import com.farmmarket.security.UserPrincipal;
import com.farmmarket.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/buyer/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * GET /api/buyer/cart
     * Get current buyer's cart with all items and total
     * Response: { items: [...], total: 45.99, itemCount: 3 }
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CartService.CartResponse>> getCart(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(cartService.getCart(principal.getId())));
    }

    /**
     * POST /api/buyer/cart
     * Add item to cart
     * Body: { productId: 1, quantity: 2 }
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CartService.CartItemResponse>> addToCart(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") Integer quantity,
            @AuthenticationPrincipal UserPrincipal principal) {
        CartService.CartItemResponse item = cartService.addToCart(principal.getId(), productId, quantity);
        return ResponseEntity.ok(ApiResponse.success("Added to cart", item));
    }

    /**
     * PUT /api/buyer/cart/{cartItemId}
     * Update quantity of a cart item
     * Body: quantity=3
     */
    @PutMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<CartService.CartItemResponse>> updateCartItem(
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity,
            @AuthenticationPrincipal UserPrincipal principal) {
        CartService.CartItemResponse item = cartService.updateCartItem(principal.getId(), cartItemId, quantity);
        return ResponseEntity.ok(ApiResponse.success("Cart updated", item));
    }

    /**
     * DELETE /api/buyer/cart/{cartItemId}
     * Remove a specific item from cart
     */
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(
            @PathVariable Long cartItemId,
            @AuthenticationPrincipal UserPrincipal principal) {
        cartService.removeFromCart(principal.getId(), cartItemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", null));
    }

    /**
     * DELETE /api/buyer/cart
     * Clear entire cart
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @AuthenticationPrincipal UserPrincipal principal) {
        cartService.clearCart(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }
}
