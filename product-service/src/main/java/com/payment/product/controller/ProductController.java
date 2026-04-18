package com.payment.product.controller;

import com.payment.common.dto.ApiResponse;
import com.payment.common.dto.PagedResponse;
import com.payment.product.dto.ProductDTO;
import com.payment.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Product Controller — REST API for product catalog management.
 * Thin controller: delegates all business logic to ProductService.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product catalog endpoints")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(@Valid @RequestBody ProductDTO dto) {
        ProductDTO created = productService.createProduct(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully", created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success("Product retrieved", product));
    }

    @GetMapping
    @Operation(summary = "Get all products with pagination")
    public ResponseEntity<ApiResponse<PagedResponse<ProductDTO>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy) {
        PagedResponse<ProductDTO> products = productService.getAllProducts(page, size, sortBy);
        return ResponseEntity.ok(ApiResponse.success("Products retrieved", products));
    }

    @GetMapping("/search")
    @Operation(summary = "Search products with filters")
    public ResponseEntity<ApiResponse<PagedResponse<ProductDTO>>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<ProductDTO> results = productService.searchProducts(name, categoryId, minPrice, maxPrice, page, size);
        return ResponseEntity.ok(ApiResponse.success("Search results", results));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a product")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
            @PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
        ProductDTO updated = productService.updateProduct(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Product updated", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete (deactivate) a product")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deactivated"));
    }
}
