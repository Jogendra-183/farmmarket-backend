package com.farmmarket.repository;

import com.farmmarket.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByBuyerId(Long buyerId);

    Optional<CartItem> findByBuyerIdAndProductId(Long buyerId, Long productId);

    void deleteByBuyerId(Long buyerId);

    void deleteByBuyerIdAndProductId(Long buyerId, Long productId);

    Long countByBuyerId(Long buyerId);

    @Query("SELECT SUM(ci.quantity * ci.unitPrice) FROM CartItem ci WHERE ci.buyer.id = :buyerId")
    BigDecimal calculateCartTotal(@Param("buyerId") Long buyerId);
}
