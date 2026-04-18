package com.payment.registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Server — Service Discovery Registry.
 * 
 * All microservices register themselves here on startup.
 * The API Gateway uses this registry to route requests dynamically.
 * 
 * Access the Eureka dashboard at: http://localhost:8761
 */
@SpringBootApplication
@EnableEurekaServer
public class ServiceRegistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceRegistryApplication.class, args);
    }
}
