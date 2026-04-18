package com.payment.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway — Single entry point for all client requests.
 * 
 * Responsibilities:
 * - Route requests to downstream microservices (via Eureka discovery)
 * - JWT token validation (gateway-level filter)
 * - Rate limiting
 * - Request/response logging
 * - CORS configuration
 * 
 * All client traffic flows: Client → Gateway (8080) → Service (808x)
 * 
 * Runs on port 8080.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
