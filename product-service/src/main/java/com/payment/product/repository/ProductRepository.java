package com.payment.product.repository;

import com.payment.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

/**
 * Product repository with custom search queries.
 * 
 * Uses Spring Data JPA's derived query methods and @Query for complex searches.
 * Pageable parameter enables transparent pagination support.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /** Find products by category ID with pagination */
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    /** Find active products only */
    Page<Product> findByActiveTrue(Pageable pageable);

    /**
     * Search products by name (case-insensitive, partial match).
     * Uses JPQL with LOWER() for case-insensitive search.
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.active = true")
    Page<Product> searchByName(@Param("name") String name, Pageable pageable);

    /**
     * Search products with multiple filters.
     * Combines name search, category filter, and price range.
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "AND p.active = true")
    Page<Product> searchProducts(
            @Param("name") String name,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);
}
