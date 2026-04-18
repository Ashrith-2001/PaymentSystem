package com.payment.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for creating a new order.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequest> items;

    private String shippingAddress;
    private String notes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {

        @NotNull(message = "Product ID is required")
        private Long productId;

        private String productName;

        @Min(value = 1, message = "Quantity must be at least 1")
        private int quantity;

        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.01", message = "Price must be positive")
        private BigDecimal unitPrice;
    }
}
