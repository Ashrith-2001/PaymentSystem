package com.payment.payment.dto;

import lombok.*;

/**
 * Result from a payment strategy execution.
 * Used internally between service and strategy layers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResult {

    private boolean success;
    private String transactionId;
    private String message;
}
