package com.payment.payment.strategy;

import com.payment.payment.dto.PaymentRequest;
import com.payment.payment.dto.PaymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * STRATEGY PATTERN — UPI Payment Implementation.
 */
@Component("upiPayment")
public class UpiPayment implements PaymentStrategy {

    private static final Logger log = LoggerFactory.getLogger(UpiPayment.class);

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        log.info("Processing UPI payment for order {} amount {}", request.getOrderId(), request.getAmount());

        try {
            Thread.sleep(200); // UPI is fast
            String transactionId = "UPI-" + UUID.randomUUID().toString().substring(0, 12);
            return PaymentResult.builder()
                    .success(true)
                    .transactionId(transactionId)
                    .message("UPI payment processed successfully")
                    .build();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return PaymentResult.builder().success(false).message("Payment interrupted").build();
        }
    }

    @Override
    public PaymentResult processRefund(String transactionId, BigDecimal amount) {
        String refundTxnId = "RF-UPI-" + UUID.randomUUID().toString().substring(0, 12);
        return PaymentResult.builder().success(true).transactionId(refundTxnId)
                .message("UPI refund processed").build();
    }
}
