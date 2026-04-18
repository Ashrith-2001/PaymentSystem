package com.payment.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Product Service — Manages product catalog and categories.
 * 
 * Features:
 * - Product CRUD with pagination and search
 * - Category management
 * - Caffeine in-memory caching for frequently accessed products
 * 
 * Runs on port 8082.
 */
@SpringBootApplication(scanBasePackages = {"com.payment.product", "com.payment.common"})
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableCaching  // Activates Spring's caching abstraction (@Cacheable, @CacheEvict)
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
