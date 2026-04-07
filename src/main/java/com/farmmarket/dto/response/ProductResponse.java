package com.farmmarket.dto.response;

import com.farmmarket.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String unit;
    private Product.Category category;
    private String imageUrl;
    private Boolean isAvailable;
    private BigDecimal rating;
    private Integer reviewCount;

    // Farmer info (embedded, not nested object for simplicity)
    private Long farmerId;
    private String farmerName;
    private String farmName;
    private String farmLocation;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
