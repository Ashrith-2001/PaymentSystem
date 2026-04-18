package com.payment.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Notification Service — Event-driven notification delivery.
 * 
 * This service is a pure Kafka CONSUMER. It does not expose REST APIs
 * for triggering notifications — all notifications are triggered by events.
 * 
 * Notification Strategy Pattern:
 * - EmailNotification: Simulates email sending
 * - SmsNotification: Simulates SMS sending
 * 
 * Runs on port 8086.
 */
@SpringBootApplication(scanBasePackages = {"com.payment.notification", "com.payment.common"})
@EnableDiscoveryClient
@EnableJpaAuditing
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
