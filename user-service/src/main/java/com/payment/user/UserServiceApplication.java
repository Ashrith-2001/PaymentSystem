package com.payment.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * User Service — Handles user registration, authentication (JWT), and profile management.
 * 
 * Features:
 * - User registration with email validation
 * - JWT-based authentication (stateless)
 * - Role-Based Access Control (ADMIN, CUSTOMER)
 * - User profile and address management
 * 
 * Runs on port 8081.
 */
@SpringBootApplication(scanBasePackages = {"com.payment.user", "com.payment.common"})
@EnableDiscoveryClient
@EnableJpaAuditing  // Enables @CreatedDate, @LastModifiedDate auto-population
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
