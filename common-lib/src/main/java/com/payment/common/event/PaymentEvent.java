package com.payment.common.event;

import com.payment.common.enums.PaymentMethod;
import com.payment.common.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Event published when a payment state changes.
 * Consumed by: Order Service (to continue/compensate SAGA), Notification Service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PaymentEvent extends BaseEvent {

    private Long paymentId;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String transactionId;
    private String failureReason;

    /**
     * Factory method: Payment completed successfully.
     */
    public static PaymentEvent paymentCompleted(Long paymentId, Long orderId, Long userId,
                                                  BigDecimal amount, PaymentMethod method,
                                                  String transactionId) {
        PaymentEvent event = new PaymentEvent();
        event.initBaseEvent("PAYMENT_COMPLETED", "payment-service");
        event.setPaymentId(paymentId);
        event.setOrderId(orderId);
        event.setUserId(userId);
        event.setAmount(amount);
        event.setPaymentMethod(method);
        event.setStatus(PaymentStatus.COMPLETED);
        event.setTransactionId(transactionId);
        return event;
    }

    /**
     * Factory method: Payment failed.
     */
    public static PaymentEvent paymentFailed(Long paymentId, Long orderId, Long userId,
                                               BigDecimal amount, String reason) {
        PaymentEvent event = new PaymentEvent();
        event.initBaseEvent("PAYMENT_FAILED", "payment-service");
        event.setPaymentId(paymentId);
        event.setOrderId(orderId);
        event.setUserId(userId);
        event.setAmount(amount);
        event.setStatus(PaymentStatus.FAILED);
        event.setFailureReason(reason);
        return event;
    }
}
