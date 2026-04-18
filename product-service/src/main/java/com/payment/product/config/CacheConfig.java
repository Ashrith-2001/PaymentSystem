package com.payment.product.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Cache Configuration using Caffeine.
 * 
 * Caffeine is a high-performance in-memory caching library for Java.
 * It provides the same @Cacheable/@CacheEvict interface as Redis
 * but runs in-process (no external server needed).
 * 
 * To switch to Redis later, just replace this config class and add
 * spring-boot-starter-data-redis dependency. No service code changes needed.
 */
@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("products", "productSearch");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(500)                    // Max 500 entries in cache
                .expireAfterWrite(10, TimeUnit.MINUTES) // Entries expire after 10 min
                .recordStats()                       // Enable cache hit/miss stats
        );
        return cacheManager;
    }
}
