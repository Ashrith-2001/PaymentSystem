package com.payment.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Order Service — Manages order lifecycle and SAGA orchestration.
 * 
 * This is the CENTRAL coordinator in the distributed transaction flow:
 * 1. Client creates an order → Order Service
 * 2. Order Service publishes ORDER_CREATED → Kafka
 * 3. Inventory Service reserves stock → publishes INVENTORY_RESERVED
 * 4. Payment Service processes payment → publishes PAYMENT_COMPLETED
 * 5. Order Service confirms order → publishes ORDER_CONFIRMED
 * 6. Notification Service sends notification
 * 
 * On failure at any step, compensating transactions are triggered.
 * 
 * Runs on port 8083.
 */
@SpringBootApplication(scanBasePackages = {"com.payment.order", "com.payment.common"})
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableAsync  // Enables @Async for non-blocking SAGA step execution
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
