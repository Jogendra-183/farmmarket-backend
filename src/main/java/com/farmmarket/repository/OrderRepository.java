package com.farmmarket.repository;

import com.farmmarket.entity.Order;
import com.farmmarket.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByBuyer(User buyer);

    List<Order> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);

    List<Order> findByStatus(Order.OrderStatus status);

    Optional<Order> findByOrderNumber(String orderNumber);

    // Farmer's orders: find orders containing products from a specific farmer
    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi WHERE oi.farmerId = :farmerId ORDER BY o.createdAt DESC")
    List<Order> findOrdersByFarmerId(@Param("farmerId") Long farmerId);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi WHERE oi.farmerId = :farmerId AND o.status = :status")
    List<Order> findOrdersByFarmerIdAndStatus(@Param("farmerId") Long farmerId,
                                              @Param("status") Order.OrderStatus status);

    // Analytics queries
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :startDate")
    Long countOrdersSince(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.paymentStatus = 'PAID' AND o.createdAt >= :startDate")
    BigDecimal sumRevenueSince(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.buyer.id = :buyerId")
    Long countByBuyerId(@Param("buyerId") Long buyerId);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.buyer.id = :buyerId AND o.paymentStatus = 'PAID'")
    BigDecimal sumTotalSpentByBuyer(@Param("buyerId") Long buyerId);

    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :start AND :end")
    List<Order> findOrdersBetween(@Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);
}
