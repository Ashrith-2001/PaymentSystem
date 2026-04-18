package com.payment.product.service;

import com.payment.common.dto.PagedResponse;
import com.payment.common.exception.BusinessException;
import com.payment.common.exception.ResourceNotFoundException;
import com.payment.product.dto.ProductDTO;
import com.payment.product.entity.Category;
import com.payment.product.entity.Product;
import com.payment.product.repository.CategoryRepository;
import com.payment.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Product Service — Core business logic for product catalog management.
 * 
 * Caching Strategy:
 * - @Cacheable: Cache product lookups (by ID, by search results)
 * - @CacheEvict: Invalidate cache on product create/update/delete
 * - Cache names: "products", "productSearch"
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Create a new product.
     * Evicts the product search cache since results may change.
     */
    @Transactional
    @CacheEvict(value = {"products", "productSearch"}, allEntries = true)
    public ProductDTO createProduct(ProductDTO dto) {
        log.info("Creating product: {}", dto.getName());

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));

        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .sku(dto.getSku())
                .imageUrl(dto.getImageUrl())
                .category(category)
                .active(true)
                .build();

        Product saved = productRepository.save(product);
        log.info("Product created with ID: {}", saved.getId());
        return mapToDTO(saved);
    }

    /**
     * Get product by ID (cached).
     * Result is cached using the product ID as key.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#productId")
    public ProductDTO getProductById(Long productId) {
        log.debug("Fetching product by ID: {} (cache miss)", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        return mapToDTO(product);
    }

    /**
     * Get all active products with pagination.
     */
    @Transactional(readOnly = true)
    public PagedResponse<ProductDTO> getAllProducts(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        Page<Product> productPage = productRepository.findByActiveTrue(pageable);
        return mapToPagedResponse(productPage);
    }

    /**
     * Search products with filters (cached for repeated searches).
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "productSearch", key = "#name + '-' + #categoryId + '-' + #minPrice + '-' + #maxPrice + '-' + #page + '-' + #size")
    public PagedResponse<ProductDTO> searchProducts(String name, Long categoryId,
                                                      BigDecimal minPrice, BigDecimal maxPrice,
                                                      int page, int size) {
        log.debug("Searching products (cache miss): name={}, categoryId={}", name, categoryId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Product> productPage = productRepository.searchProducts(name, categoryId, minPrice, maxPrice, pageable);
        return mapToPagedResponse(productPage);
    }

    /**
     * Update an existing product.
     * Evicts the specific product cache and search cache.
     */
    @Transactional
    @CacheEvict(value = {"products", "productSearch"}, allEntries = true)
    public ProductDTO updateProduct(Long productId, ProductDTO dto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setSku(dto.getSku());
        product.setImageUrl(dto.getImageUrl());

        if (dto.getCategoryId() != null && !dto.getCategoryId().equals(product.getCategory().getId())) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));
            product.setCategory(category);
        }

        Product updated = productRepository.save(product);
        log.info("Product {} updated", productId);
        return mapToDTO(updated);
    }

    /**
     * Soft-delete a product (set active = false).
     */
    @Transactional
    @CacheEvict(value = {"products", "productSearch"}, allEntries = true)
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        product.setActive(false);
        productRepository.save(product);
        log.info("Product {} deactivated", productId);
    }

    // ---- Mapping helpers ----

    private ProductDTO mapToDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .sku(product.getSku())
                .imageUrl(product.getImageUrl())
                .active(product.isActive())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .build();
    }

    private PagedResponse<ProductDTO> mapToPagedResponse(Page<Product> page) {
        return PagedResponse.<ProductDTO>builder()
                .content(page.getContent().stream().map(this::mapToDTO).toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }
}
