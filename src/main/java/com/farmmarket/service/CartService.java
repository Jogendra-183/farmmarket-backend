package com.farmmarket.service;

import com.farmmarket.dto.response.ProductResponse;
import com.farmmarket.entity.CartItem;
import com.farmmarket.entity.Product;
import com.farmmarket.entity.User;
import com.farmmarket.exception.BadRequestException;
import com.farmmarket.exception.ResourceNotFoundException;
import com.farmmarket.repository.CartItemRepository;
import com.farmmarket.repository.ProductRepository;
import com.farmmarket.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductService productService;

    public CartResponse getCart(Long buyerId) {
        List<CartItem> items = cartItemRepository.findByBuyerId(buyerId);
        BigDecimal total = cartItemRepository.calculateCartTotal(buyerId);
        if (total == null) total = BigDecimal.ZERO;

        List<CartItemResponse> responseItems = items.stream()
                .map(this::mapToCartItemResponse)
                .collect(Collectors.toList());

        return CartResponse.builder()
                .items(responseItems)
                .total(total)
                .itemCount(items.stream().mapToInt(CartItem::getQuantity).sum())
                .build();
    }

    @Transactional
    public CartItemResponse addToCart(Long buyerId, Long productId, Integer quantity) {
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", buyerId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (!product.getIsAvailable()) {
            throw new BadRequestException("Product is not available");
        }

        if (product.getStock() < quantity) {
            throw new BadRequestException("Insufficient stock. Available: " + product.getStock());
        }

        Optional<CartItem> existing = cartItemRepository.findByBuyerIdAndProductId(buyerId, productId);

        CartItem cartItem;
        if (existing.isPresent()) {
            cartItem = existing.get();
            int newQty = cartItem.getQuantity() + quantity;
            if (product.getStock() < newQty) {
                throw new BadRequestException("Insufficient stock for requested quantity. Available: " + product.getStock());
            }
            cartItem.setQuantity(newQty);
        } else {
            cartItem = CartItem.builder()
                    .buyer(buyer)
                    .product(product)
                    .quantity(quantity)
                    .unitPrice(product.getPrice())
                    .build();
        }

        return mapToCartItemResponse(cartItemRepository.save(cartItem));
    }

    @Transactional
    public CartItemResponse updateCartItem(Long buyerId, Long cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));

        if (!cartItem.getBuyer().getId().equals(buyerId)) {
            throw new BadRequestException("Cart item does not belong to this user");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
            return null;
        }

        if (cartItem.getProduct().getStock() < quantity) {
            throw new BadRequestException("Insufficient stock. Available: " + cartItem.getProduct().getStock());
        }

        cartItem.setQuantity(quantity);
        return mapToCartItemResponse(cartItemRepository.save(cartItem));
    }

    @Transactional
    public void removeFromCart(Long buyerId, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));

        if (!cartItem.getBuyer().getId().equals(buyerId)) {
            throw new BadRequestException("Cart item does not belong to this user");
        }
        cartItemRepository.delete(cartItem);
    }

    @Transactional
    public void clearCart(Long buyerId) {
        cartItemRepository.deleteByBuyerId(buyerId);
    }

    private CartItemResponse mapToCartItemResponse(CartItem item) {
        ProductResponse productResponse = productService.mapToResponse(item.getProduct());
        return CartItemResponse.builder()
                .id(item.getId())
                .product(productResponse)
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .build();
    }

    // Inner DTOs
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CartResponse {
        private List<CartItemResponse> items;
        private BigDecimal total;
        private Integer itemCount;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CartItemResponse {
        private Long id;
        private ProductResponse product;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }
}
