package com.farmmarket.repository;

import com.farmmarket.entity.Product;
import com.farmmarket.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByFarmer(User farmer);

    List<Product> findByFarmerId(Long farmerId);

    List<Product> findByCategory(Product.Category category);

    List<Product> findByIsAvailable(Boolean isAvailable);

    Page<Product> findByIsAvailableTrue(Pageable pageable);

    Page<Product> findByCategoryAndIsAvailableTrue(Product.Category category, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isAvailable = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isAvailable = true AND p.category = :category AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchProductsByCategory(@Param("keyword") String keyword,
                                           @Param("category") Product.Category category,
                                           Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.stock <= :threshold AND p.isAvailable = true")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.farmer.id = :farmerId")
    Long countByFarmerId(@Param("farmerId") Long farmerId);

    List<Product> findByFarmerIdOrderByCreatedAtDesc(Long farmerId);

    @Query("SELECT p FROM Product p ORDER BY p.reviewCount DESC")
    List<Product> findTopRatedProducts(Pageable pageable);
}
