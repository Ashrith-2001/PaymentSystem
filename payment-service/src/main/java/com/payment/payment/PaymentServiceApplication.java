package com.payment.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Payment Service — Processes payments using the Strategy pattern.
 * 
 * Features:
 * - Multiple payment method support (Strategy pattern)
 * - Idempotency enforcement (prevents double-charging)
 * - Refund processing
 * - Kafka integration for SAGA events
 * 
 * Runs on port 8084.
 */
@SpringBootApplication(scanBasePackages = {"com.payment.payment", "com.payment.common"})
@EnableDiscoveryClient
@EnableJpaAuditing
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
