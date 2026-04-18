package com.payment.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Inventory Service — Manages product stock levels and reservations.
 * 
 * Features:
 * - Stock management with pessimistic locking (prevents overselling)
 * - Stock reservation for in-progress orders
 * - Kafka consumer for order events, producer for inventory events
 * - Compensating transaction: stock release on order cancellation
 * 
 * Runs on port 8085.
 */
@SpringBootApplication(scanBasePackages = {"com.payment.inventory", "com.payment.common"})
@EnableDiscoveryClient
@EnableJpaAuditing
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}
