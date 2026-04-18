package com.payment.payment.service;

import com.payment.common.enums.PaymentStatus;
import com.payment.common.exception.BusinessException;
import com.payment.common.exception.DuplicateRequestException;
import com.payment.common.exception.ResourceNotFoundException;
import com.payment.payment.dto.PaymentRequest;
import com.payment.payment.dto.PaymentResponse;
import com.payment.payment.dto.PaymentResult;
import com.payment.payment.entity.Payment;
import com.payment.payment.repository.PaymentRepository;
import com.payment.payment.strategy.PaymentStrategy;
import com.payment.payment.strategy.PaymentStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Payment Service — Orchestrates payment processing using Strategy pattern.
 * 
 * Key features:
 * 1. Strategy Pattern: Delegates to the correct payment processor based on PaymentMethod
 * 2. Factory Pattern: PaymentStrategyFactory resolves the strategy
 * 3. Idempotency: Duplicate requests with same idempotency key return original response
 * 4. Pessimistic Locking: Prevents concurrent payment processing for the same order
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentStrategyFactory strategyFactory;

    /**
     * Process a payment for an order.
     * 
     * Flow:
     * 1. Check idempotency key — if duplicate, return existing payment
     * 2. Create Payment record with INITIATED status
     * 3. Get the appropriate strategy from the factory
     * 4. Execute payment via strategy
     * 5. Update Payment record with result
     */
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for order {} via {}", request.getOrderId(), request.getPaymentMethod());

        // Step 1: Idempotency check — prevent double-charging
        if (request.getIdempotencyKey() != null) {
            Payment existing = paymentRepository.findByIdempotencyKey(request.getIdempotencyKey())
                    .orElse(null);
            if (existing != null) {
                log.info("Duplicate request detected (idempotency key: {}). Returning existing payment.",
                        request.getIdempotencyKey());
                return mapToResponse(existing);
            }
        }

        // Check if payment already exists for this order
        paymentRepository.findByOrderId(request.getOrderId()).ifPresent(existing -> {
            if (existing.getStatus() == PaymentStatus.COMPLETED) {
                throw new BusinessException(
                        "Payment already completed for order " + request.getOrderId(),
                        "ERR_PAYMENT_EXISTS");
            }
        });

        // Step 2: Create payment record
        String idempotencyKey = request.getIdempotencyKey() != null
                ? request.getIdempotencyKey()
                : UUID.randomUUID().toString();

        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.PROCESSING)
                .idempotencyKey(idempotencyKey)
                .build();
        payment = paymentRepository.save(payment);

        // Step 3: Get the strategy for this payment method (Factory Pattern)
        PaymentStrategy strategy = strategyFactory.getStrategy(request.getPaymentMethod());

        // Step 4: Execute payment (Strategy Pattern — polymorphic dispatch)
        PaymentResult result = strategy.processPayment(request);

        // Step 5: Update payment record based on result
        if (result.isSuccess()) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setTransactionId(result.getTransactionId());
            log.info("Payment {} completed for order {}. Transaction: {}",
                    payment.getId(), request.getOrderId(), result.getTransactionId());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(result.getMessage());
            log.warn("Payment {} failed for order {}: {}",
                    payment.getId(), request.getOrderId(), result.getMessage());
        }

        payment = paymentRepository.save(payment);
        return mapToResponse(payment);
    }

    /**
     * Get payment details by ID.
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));
        return mapToResponse(payment);
    }

    /**
     * Get payment by order ID.
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", orderId));
        return mapToResponse(payment);
    }

    /**
     * Get all payments for a user.
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Process a refund for a completed payment.
     */
    @Transactional
    public PaymentResponse refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new BusinessException("Can only refund completed payments", "ERR_INVALID_REFUND");
        }

        PaymentStrategy strategy = strategyFactory.getStrategy(payment.getPaymentMethod());
        PaymentResult result = strategy.processRefund(payment.getTransactionId(), payment.getAmount());

        if (result.isSuccess()) {
            payment.setStatus(PaymentStatus.REFUNDED);
            log.info("Payment {} refunded. Refund transaction: {}", paymentId, result.getTransactionId());
        } else {
            throw new BusinessException("Refund failed: " + result.getMessage(), "ERR_REFUND_FAILED");
        }

        payment = paymentRepository.save(payment);
        return mapToResponse(payment);
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
