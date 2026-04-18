package com.payment.payment.strategy;

import com.payment.payment.dto.PaymentRequest;
import com.payment.payment.dto.PaymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * STRATEGY PATTERN — Wallet Payment Implementation.
 * Simulates digital wallet payment (e.g., Google Pay, Apple Pay).
 */
@Component("walletPayment")
public class WalletPayment implements PaymentStrategy {

    private static final Logger log = LoggerFactory.getLogger(WalletPayment.class);

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        log.info("Processing WALLET payment for order {} amount {}", request.getOrderId(), request.getAmount());

        try {
            Thread.sleep(300); // Wallets are typically faster

            // Simulate balance check
            if (request.getAmount().compareTo(new BigDecimal("10000")) > 0) {
                return PaymentResult.builder()
                        .success(false)
                        .message("Wallet balance insufficient")
                        .build();
            }

            String transactionId = "WL-" + UUID.randomUUID().toString().substring(0, 12);
            log.info("Wallet payment successful. Transaction: {}", transactionId);

            return PaymentResult.builder()
                    .success(true)
                    .transactionId(transactionId)
                    .message("Wallet payment processed successfully")
                    .build();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return PaymentResult.builder().success(false).message("Payment interrupted").build();
        }
    }

    @Override
    public PaymentResult processRefund(String transactionId, BigDecimal amount) {
        log.info("Processing WALLET refund for transaction {}", transactionId);
        String refundTxnId = "RF-WL-" + UUID.randomUUID().toString().substring(0, 12);
        return PaymentResult.builder().success(true).transactionId(refundTxnId)
                .message("Wallet refund credited").build();
    }
}
