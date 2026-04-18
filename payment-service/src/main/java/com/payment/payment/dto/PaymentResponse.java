package com.payment.payment.dto;

import com.payment.common.enums.PaymentMethod;
import com.payment.common.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for payment details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long id;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String transactionId;
    private String failureReason;
    private LocalDateTime createdAt;
}
