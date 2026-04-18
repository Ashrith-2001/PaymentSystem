package com.payment.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global logging filter — logs all requests passing through the gateway.
 * Useful for debugging and monitoring request flow.
 */
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        log.info("→ Gateway Request: {} {} (from: {})",
                request.getMethod(),
                request.getURI().getPath(),
                request.getRemoteAddress());

        long startTime = System.currentTimeMillis();

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime;
            log.info("← Gateway Response: {} {} → {} ({}ms)",
                    request.getMethod(),
                    request.getURI().getPath(),
                    exchange.getResponse().getStatusCode(),
                    duration);
        }));
    }

    @Override
    public int getOrder() {
        return -2; // Run before auth filter
    }
}
