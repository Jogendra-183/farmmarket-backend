package com.farmmarket.service;

import com.farmmarket.dto.request.ProductRequest;
import com.farmmarket.dto.response.ProductResponse;
import com.farmmarket.entity.Product;
import com.farmmarket.entity.User;
import com.farmmarket.exception.BadRequestException;
import com.farmmarket.exception.ResourceNotFoundException;
import com.farmmarket.exception.UnauthorizedException;
import com.farmmarket.repository.ProductRepository;
import com.farmmarket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // ==================== Public / Buyer ====================

    public Page<ProductResponse> getAllAvailableProducts(int page, int size, String sort) {
        Sort sortOrder = switch (sort) {
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "rating" -> Sort.by("rating").descending();
            default -> Sort.by("createdAt").descending();
        };
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        return productRepository.findByIsAvailableTrue(pageable).map(this::mapToResponse);
    }

    public Page<ProductResponse> searchProducts(String keyword, String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if (keyword != null && !keyword.isBlank() && category != null && !category.equalsIgnoreCase("all")) {
            Product.Category cat = parseCategory(category);
            return productRepository.searchProductsByCategory(keyword, cat, pageable).map(this::mapToResponse);
        } else if (keyword != null && !keyword.isBlank()) {
            return productRepository.searchProducts(keyword, pageable).map(this::mapToResponse);
        } else if (category != null && !category.equalsIgnoreCase("all")) {
            Product.Category cat = parseCategory(category);
            return productRepository.findByCategoryAndIsAvailableTrue(cat, pageable).map(this::mapToResponse);
        }
        return productRepository.findByIsAvailableTrue(pageable).map(this::mapToResponse);
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return mapToResponse(product);
    }

    public List<ProductResponse> getTopRatedProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findTopRatedProducts(pageable)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ==================== Farmer ====================

    public List<ProductResponse> getFarmerProducts(Long farmerId) {
        return productRepository.findByFarmerIdOrderByCreatedAtDesc(farmerId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public ProductResponse createProduct(Long farmerId, ProductRequest request) {
        User farmer = userRepository.findById(farmerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", farmerId));

        if (farmer.getRole() != User.Role.FARMER) {
            throw new UnauthorizedException("Only farmers can create products");
        }

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .unit(request.getUnit())
                .category(request.getCategory())
                .imageUrl(request.getImageUrl())
                .isAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true)
                .farmer(farmer)
                .build();

        return mapToResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse updateProduct(Long farmerId, Long productId, ProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (!product.getFarmer().getId().equals(farmerId)) {
            throw new UnauthorizedException("You can only update your own products");
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setUnit(request.getUnit());
        product.setCategory(request.getCategory());
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }
        if (request.getIsAvailable() != null) {
            product.setIsAvailable(request.getIsAvailable());
        }

        return mapToResponse(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long farmerId, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (!product.getFarmer().getId().equals(farmerId)) {
            throw new UnauthorizedException("You can only delete your own products");
        }

        productRepository.delete(product);
    }

    public List<ProductResponse> getLowStockProducts(Long farmerId) {
        return productRepository.findLowStockProducts(5)
                .stream()
                .filter(p -> p.getFarmer().getId().equals(farmerId))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ==================== Admin ====================

    public Page<ProductResponse> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return productRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional
    public void adminDeleteProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }
        productRepository.deleteById(productId);
    }

    @Transactional
    public ProductResponse toggleProductAvailability(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        product.setIsAvailable(!product.getIsAvailable());
        return mapToResponse(productRepository.save(product));
    }

    // ==================== Helpers ====================

    private Product.Category parseCategory(String category) {
        try {
            return Product.Category.valueOf(category.toUpperCase().replace(" & ", "_AND_").replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid category: " + category);
        }
    }

    public ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .unit(product.getUnit())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .isAvailable(product.getIsAvailable())
                .rating(product.getRating())
                .reviewCount(product.getReviewCount())
                .farmerId(product.getFarmer().getId())
                .farmerName(product.getFarmer().getName())
                .farmName(product.getFarmer().getFarmName())
                .farmLocation(product.getFarmer().getFarmLocation())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
