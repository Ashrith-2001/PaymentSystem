package com.payment.order.dto;

import com.payment.common.enums.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for order details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    private Long id;
    private Long userId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String notes;
    private List<OrderItemDTO> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDTO {
        private Long id;
        private Long productId;
        private String productName;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }
}
