package com.payment.payment.strategy;

import com.payment.payment.dto.PaymentRequest;
import com.payment.payment.dto.PaymentResult;

/**
 * STRATEGY PATTERN — Payment Strategy Interface.
 * 
 * Each payment method (Credit Card, Debit Card, Wallet, etc.)
 * implements this interface with its own processing logic.
 * 
 * The concrete strategy is selected at runtime by PaymentStrategyFactory
 * based on the PaymentMethod specified in the request.
 * 
 * This allows adding new payment methods by:
 * 1. Creating a new class implementing PaymentStrategy
 * 2. Registering it in PaymentStrategyFactory
 * (Open/Closed Principle — open for extension, closed for modification)
 */
public interface PaymentStrategy {

    /**
     * Process a payment using this specific strategy.
     * 
     * @param request the payment request details
     * @return PaymentResult with success/failure and transaction ID
     */
    PaymentResult processPayment(PaymentRequest request);

    /**
     * Process a refund for a previously completed payment.
     * 
     * @param transactionId the original transaction ID
     * @param amount the amount to refund
     * @return PaymentResult for the refund operation
     */
    PaymentResult processRefund(String transactionId, java.math.BigDecimal amount);
}
