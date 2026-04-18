package com.payment.payment.dto;

import com.payment.common.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/**
 * Request DTO for initiating a payment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    /**
     * Idempotency key — client-generated unique key per payment attempt.
     * If the same key is sent again, the original response is returned.
     * This prevents double-charging due to network retries.
     */
    private String idempotencyKey;
}
