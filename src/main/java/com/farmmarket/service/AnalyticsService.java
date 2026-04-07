package com.farmmarket.service;

import com.farmmarket.dto.response.OrderResponse;
import com.farmmarket.dto.response.ProductResponse;
import com.farmmarket.entity.Order;
import com.farmmarket.entity.User;
import com.farmmarket.repository.OrderRepository;
import com.farmmarket.repository.ProductRepository;
import com.farmmarket.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductService productService;
    private final OrderService orderService;

    // Farmer Analytics
    public FarmerAnalyticsResponse getFarmerAnalytics(Long farmerId) {
        List<Order> farmerOrders = orderRepository.findOrdersByFarmerId(farmerId);

        BigDecimal totalRevenue = farmerOrders.stream()
                .filter(o -> o.getPaymentStatus() == Order.PaymentStatus.PAID)
                .flatMap(o -> o.getOrderItems().stream())
                .filter(item -> item.getFarmerId().equals(farmerId))
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalOrders = farmerOrders.size();
        long totalProducts = productRepository.countByFarmerId(farmerId);

        long pendingOrders = farmerOrders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.PENDING || o.getStatus() == Order.OrderStatus.PROCESSING)
                .count();

        List<ProductResponse> lowStockProducts = productService.getLowStockProducts(farmerId);

        // Monthly revenue map (last 6 months)
        Map<String, BigDecimal> monthlyRevenue = farmerOrders.stream()
                .filter(o -> o.getPaymentStatus() == Order.PaymentStatus.PAID
                        && o.getCreatedAt().isAfter(LocalDateTime.now().minusMonths(6)))
                .collect(Collectors.groupingBy(
                        o -> o.getCreatedAt().getMonth().name(),
                        Collectors.reducing(BigDecimal.ZERO,
                                o -> o.getOrderItems().stream()
                                        .filter(item -> item.getFarmerId().equals(farmerId))
                                        .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                                        .reduce(BigDecimal.ZERO, BigDecimal::add),
                                BigDecimal::add)
                ));

        return FarmerAnalyticsResponse.builder()
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .totalProducts(totalProducts)
                .pendingOrders(pendingOrders)
                .lowStockCount((long) lowStockProducts.size())
                .lowStockProducts(lowStockProducts)
                .monthlyRevenue(monthlyRevenue)
                .build();
    }

    // Admin Analytics
    public AdminAnalyticsResponse getAdminAnalytics() {
        long totalUsers = userRepository.count();
        long totalFarmers = userRepository.countByRole(User.Role.FARMER);
        long totalBuyers = userRepository.countByRole(User.Role.BUYER);
        long totalProducts = productRepository.count();

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long recentOrders = orderRepository.countOrdersSince(thirtyDaysAgo);
        BigDecimal recentRevenue = orderRepository.sumRevenueSince(thirtyDaysAgo);
        if (recentRevenue == null) recentRevenue = BigDecimal.ZERO;

        List<Order> allOrders = orderRepository.findAll();
        BigDecimal totalRevenue = allOrders.stream()
                .filter(o -> o.getPaymentStatus() == Order.PaymentStatus.PAID)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalOrders = allOrders.size();

        Map<String, Long> ordersByStatus = allOrders.stream()
                .collect(Collectors.groupingBy(o -> o.getStatus().name(), Collectors.counting()));

        return AdminAnalyticsResponse.builder()
                .totalUsers(totalUsers)
                .totalFarmers(totalFarmers)
                .totalBuyers(totalBuyers)
                .totalProducts(totalProducts)
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .recentOrders(recentOrders)
                .recentRevenue(recentRevenue)
                .ordersByStatus(ordersByStatus)
                .build();
    }

    // Buyer Dashboard Stats
    public BuyerDashboardResponse getBuyerDashboard(Long buyerId) {
        long totalOrders = orderRepository.countByBuyerId(buyerId);
        BigDecimal totalSpent = orderRepository.sumTotalSpentByBuyer(buyerId);
        if (totalSpent == null) totalSpent = BigDecimal.ZERO;

        List<OrderResponse> recentOrders = orderRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId)
                .stream().limit(5).map(orderService::mapToResponse).collect(Collectors.toList());

        return BuyerDashboardResponse.builder()
                .totalOrders(totalOrders)
                .totalSpent(totalSpent)
                .recentOrders(recentOrders)
                .build();
    }

    // DTOs
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class FarmerAnalyticsResponse {
        private BigDecimal totalRevenue;
        private long totalOrders;
        private long totalProducts;
        private long pendingOrders;
        private long lowStockCount;
        private List<ProductResponse> lowStockProducts;
        private Map<String, BigDecimal> monthlyRevenue;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AdminAnalyticsResponse {
        private long totalUsers;
        private long totalFarmers;
        private long totalBuyers;
        private long totalProducts;
        private long totalOrders;
        private BigDecimal totalRevenue;
        private long recentOrders;
        private BigDecimal recentRevenue;
        private Map<String, Long> ordersByStatus;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class BuyerDashboardResponse {
        private long totalOrders;
        private BigDecimal totalSpent;
        private List<OrderResponse> recentOrders;
    }
}
