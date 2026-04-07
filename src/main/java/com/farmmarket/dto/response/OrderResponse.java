package com.farmmarket.dto.response;

import com.farmmarket.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private String orderNumber;
    private Order.OrderStatus status;
    private Order.PaymentStatus paymentStatus;
    private BigDecimal totalAmount;

    // Buyer info
    private Long buyerId;
    private String buyerName;
    private String buyerEmail;

    // Shipping
    private String shippingAddress;
    private String shippingCity;
    private String shippingState;
    private String shippingZip;

    private String paymentMethod;
    private String notes;

    private List<OrderItemResponse> orderItems;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private String productImageUrl;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private Long farmerId;
        private String farmerName;
    }
}
