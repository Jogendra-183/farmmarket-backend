package com.farmmarket.service;

import com.farmmarket.dto.request.OrderRequest;
import com.farmmarket.dto.response.OrderResponse;
import com.farmmarket.entity.*;
import com.farmmarket.exception.BadRequestException;
import com.farmmarket.exception.ResourceNotFoundException;
import com.farmmarket.repository.CartItemRepository;
import com.farmmarket.repository.NotificationRepository;
import com.farmmarket.repository.OrderRepository;
import com.farmmarket.repository.ProductRepository;
import com.farmmarket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final NotificationRepository notificationRepository;

    // ==================== Buyer ====================

    @Transactional
    public OrderResponse placeOrder(Long buyerId, OrderRequest request) {
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", buyerId));

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemReq.getProductId()));

            if (!product.getIsAvailable()) {
                throw new BadRequestException("Product '" + product.getName() + "' is not available");
            }
            if (product.getStock() < itemReq.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product: " + product.getName()
                        + ". Available: " + product.getStock());
            }

            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(product.getPrice())
                    .totalPrice(itemTotal)
                    .farmerId(product.getFarmer().getId())
                    .farmerName(product.getFarmer().getName())
                    .build();
            orderItems.add(orderItem);

            // Deduct stock
            product.setStock(product.getStock() - itemReq.getQuantity());
            productRepository.save(product);
        }

        Order order = Order.builder()
                .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .buyer(buyer)
                .status(Order.OrderStatus.PENDING)
                .paymentStatus(Order.PaymentStatus.PAID)
                .totalAmount(totalAmount)
                .shippingAddress(request.getShippingAddress())
                .shippingCity(request.getShippingCity())
                .shippingState(request.getShippingState())
                .shippingZip(request.getShippingZip())
                .paymentMethod(request.getPaymentMethod())
                .notes(request.getNotes())
                .build();

        final Order savedOrder = orderRepository.save(order);
        orderItems.forEach(item -> item.setOrder(savedOrder));
        savedOrder.setOrderItems(orderItems);
        orderRepository.save(savedOrder);

        // Clear buyer's cart
        cartItemRepository.deleteByBuyerId(buyerId);

        // Send notification to buyer
        createNotification(buyer, "Order Placed Successfully",
                "Your order #" + savedOrder.getOrderNumber() + " has been placed.",
                Notification.NotificationType.ORDER_PLACED, savedOrder.getId());

        return mapToResponse(savedOrder);
    }

    public List<OrderResponse> getBuyerOrders(Long buyerId) {
        return orderRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public OrderResponse getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return mapToResponse(order);
    }

    // ==================== Farmer ====================

    public List<OrderResponse> getFarmerOrders(Long farmerId) {
        return orderRepository.findOrdersByFarmerId(farmerId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<OrderResponse> getFarmerOrdersByStatus(Long farmerId, Order.OrderStatus status) {
        return orderRepository.findOrdersByFarmerIdAndStatus(farmerId, status)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, Order.OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);

        // Notify buyer on status change
        Notification.NotificationType type = switch (newStatus) {
            case SHIPPED -> Notification.NotificationType.ORDER_SHIPPED;
            case DELIVERED -> Notification.NotificationType.ORDER_DELIVERED;
            case CANCELLED -> Notification.NotificationType.ORDER_CANCELLED;
            default -> Notification.NotificationType.SYSTEM;
        };
        createNotification(saved.getBuyer(),
                "Order Status Updated",
                "Your order #" + saved.getOrderNumber() + " is now " + newStatus.name(),
                type, saved.getId());

        return mapToResponse(saved);
    }

    // ==================== Admin ====================

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ==================== Helpers ====================

    private void createNotification(User user, String title, String message,
                                    Notification.NotificationType type, Long relatedId) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .relatedEntityId(relatedId)
                .relatedEntityType("ORDER")
                .build();
        notificationRepository.save(notification);
    }

    public OrderResponse mapToResponse(Order order) {
        List<OrderResponse.OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(item -> OrderResponse.OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .productImageUrl(item.getProduct().getImageUrl())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getTotalPrice())
                        .farmerId(item.getFarmerId())
                        .farmerName(item.getFarmerName())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .totalAmount(order.getTotalAmount())
                .buyerId(order.getBuyer().getId())
                .buyerName(order.getBuyer().getName())
                .buyerEmail(order.getBuyer().getEmail())
                .shippingAddress(order.getShippingAddress())
                .shippingCity(order.getShippingCity())
                .shippingState(order.getShippingState())
                .shippingZip(order.getShippingZip())
                .paymentMethod(order.getPaymentMethod())
                .notes(order.getNotes())
                .orderItems(itemResponses)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
