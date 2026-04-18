package com.payment.payment.entity;

import com.payment.common.enums.PaymentMethod;
import com.payment.common.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment entity — represents a payment against an order.
 */
@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payments_order", columnList = "orderId"),
        @Index(name = "idx_payments_idempotency", columnList = "idempotencyKey", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.INITIATED;

    /** Unique transaction ID from the payment gateway */
    @Column(length = 100)
    private String transactionId;

    /**
     * Idempotency key — prevents duplicate payments.
     * Client sends this with the payment request.
     * If the same key is received again, the original response is returned.
     */
    @Column(unique = true, length = 100)
    private String idempotencyKey;

    /** Reason for payment failure (if applicable) */
    @Column(length = 500)
    private String failureReason;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
