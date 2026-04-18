package com.payment.payment.strategy;

import com.payment.payment.dto.PaymentRequest;
import com.payment.payment.dto.PaymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * STRATEGY PATTERN — Credit Card Payment Implementation.
 * 
 * Simulates credit card payment processing.
 * In production, this would integrate with a real payment gateway (Stripe, Razorpay).
 * The interface remains the same — only the implementation changes.
 */
@Component("creditCardPayment")
public class CreditCardPayment implements PaymentStrategy {

    private static final Logger log = LoggerFactory.getLogger(CreditCardPayment.class);

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        log.info("Processing CREDIT_CARD payment for order {} amount {}",
                request.getOrderId(), request.getAmount());

        // Simulate payment gateway call
        // In production: call Stripe API, Razorpay API, etc.
        try {
            // Simulate processing delay
            Thread.sleep(500);

            // Simulate 90% success rate
            if (Math.random() > 0.1) {
                String transactionId = "CC-" + UUID.randomUUID().toString().substring(0, 12);
                log.info("Credit card payment successful. Transaction: {}", transactionId);

                return PaymentResult.builder()
                        .success(true)
                        .transactionId(transactionId)
                        .message("Credit card payment processed successfully")
                        .build();
            } else {
                log.warn("Credit card payment declined for order {}", request.getOrderId());
                return PaymentResult.builder()
                        .success(false)
                        .message("Credit card payment declined by issuer")
                        .build();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return PaymentResult.builder()
                    .success(false)
                    .message("Payment processing interrupted")
                    .build();
        }
    }

    @Override
    public PaymentResult processRefund(String transactionId, BigDecimal amount) {
        log.info("Processing CREDIT_CARD refund for transaction {}, amount {}", transactionId, amount);
        String refundTxnId = "RF-CC-" + UUID.randomUUID().toString().substring(0, 12);
        return PaymentResult.builder()
                .success(true)
                .transactionId(refundTxnId)
                .message("Credit card refund processed")
                .build();
    }
}
