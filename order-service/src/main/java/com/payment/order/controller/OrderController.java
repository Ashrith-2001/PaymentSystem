package com.payment.order.controller;

import com.payment.common.dto.ApiResponse;
import com.payment.order.dto.CreateOrderRequest;
import com.payment.order.dto.OrderDTO;
import com.payment.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {

    private final OrderService orderService;

    /**
     * Create a new order.
     * Returns 202 Accepted because order processing is async (SAGA).
     */
    @PostMapping
    @Operation(summary = "Create a new order (initiates SAGA)")
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderDTO order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success("Order created. Processing initiated.", order));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderById(@PathVariable Long id) {
        OrderDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success("Order retrieved", order));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all orders for a user")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getOrdersByUser(@PathVariable Long userId) {
        List<OrderDTO> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved", orders));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel an order")
    public ResponseEntity<ApiResponse<OrderDTO>> cancelOrder(
            @PathVariable Long id,
            @RequestParam(defaultValue = "Cancelled by user") String reason) {
        OrderDTO order = orderService.cancelOrder(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled", order));
    }
}
