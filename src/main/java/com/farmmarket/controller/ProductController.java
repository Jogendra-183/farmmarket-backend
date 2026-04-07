package com.farmmarket.controller;

import com.farmmarket.dto.request.ProductRequest;
import com.farmmarket.dto.response.ApiResponse;
import com.farmmarket.dto.response.ProductResponse;
import com.farmmarket.security.UserPrincipal;
import com.farmmarket.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ============================
    // PUBLIC endpoints
    // ============================

    /**
     * GET /api/products?page=0&size=12&sort=newest
     * Browse all available products (buyer browse page)
     */
    @GetMapping("/api/products")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "newest") String sort) {
        return ResponseEntity.ok(ApiResponse.success(
                productService.getAllAvailableProducts(page, size, sort)));
    }

    /**
     * GET /api/products/search?keyword=tomato&category=vegetables&page=0&size=12
     * Search products with optional keyword and category filter
     */
    @GetMapping("/api/products/search")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "all") String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                productService.searchProducts(keyword, category, page, size)));
    }

    /**
     * GET /api/products/{id}
     * Get single product details
     */
    @GetMapping("/api/products/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductById(id)));
    }

    /**
     * GET /api/products/top-rated?limit=3
     * Get top rated products for AI recommendations section on browse page
     */
    @GetMapping("/api/products/top-rated")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getTopRated(
            @RequestParam(defaultValue = "3") int limit) {
        return ResponseEntity.ok(ApiResponse.success(productService.getTopRatedProducts(limit)));
    }

    // ============================
    // FARMER endpoints
    // ============================

    /**
     * GET /api/farmer/products
     * Get farmer's own products
     */
    @GetMapping("/api/farmer/products")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getFarmerProducts(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                productService.getFarmerProducts(principal.getId())));
    }

    /**
     * POST /api/farmer/products
     * Create a new product listing
     * Body: { name, description, price, stock, unit, category, imageUrl }
     */
    @PostMapping("/api/farmer/products")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        ProductResponse product = productService.createProduct(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully", product));
    }

    /**
     * PUT /api/farmer/products/{id}
     * Update an existing product
     */
    @PutMapping("/api/farmer/products/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully",
                productService.updateProduct(principal.getId(), id, request)));
    }

    /**
     * DELETE /api/farmer/products/{id}
     * Delete a product listing
     */
    @DeleteMapping("/api/farmer/products/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        productService.deleteProduct(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", null));
    }

    /**
     * GET /api/farmer/products/low-stock
     * Get products with low stock (for analytics alerts)
     */
    @GetMapping("/api/farmer/products/low-stock")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getLowStockProducts(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                productService.getLowStockProducts(principal.getId())));
    }

    // ============================
    // ADMIN endpoints
    // ============================

    /**
     * GET /api/admin/products?page=0&size=20
     * Admin view of all products
     */
    @GetMapping("/api/admin/products")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProductsAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(productService.getAllProducts(page, size)));
    }

    /**
     * DELETE /api/admin/products/{id}
     * Admin delete any product
     */
    @DeleteMapping("/api/admin/products/{id}")
    public ResponseEntity<ApiResponse<Void>> adminDeleteProduct(@PathVariable Long id) {
        productService.adminDeleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product removed", null));
    }

    /**
     * PATCH /api/admin/products/{id}/toggle-availability
     * Admin toggle product availability
     */
    @PatchMapping("/api/admin/products/{id}/toggle-availability")
    public ResponseEntity<ApiResponse<ProductResponse>> toggleAvailability(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.toggleProductAvailability(id)));
    }
}
