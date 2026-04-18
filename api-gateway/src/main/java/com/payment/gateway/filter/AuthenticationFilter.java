package com.payment.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Gateway-level JWT validation filter.
 * 
 * This filter checks for the presence of a JWT token in the Authorization header
 * for protected routes. It does NOT validate the token contents (that's done by
 * each service's own security layer) — it only ensures the header exists.
 * 
 * Public routes (auth, swagger, actuator) bypass this filter.
 */
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

    /** Routes that don't require authentication */
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth",
            "/swagger-ui",
            "/v3/api-docs",
            "/actuator",
            "/eureka"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Skip authentication for public paths
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // Check for Authorization header
        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            log.warn("Missing Authorization header for request: {} {}", request.getMethod(), path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Invalid Authorization header format for: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        log.debug("Request authenticated (Bearer token present): {} {}", request.getMethod(), path);
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1; // Run before other filters
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
}
