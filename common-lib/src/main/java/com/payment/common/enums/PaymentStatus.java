package com.payment.common.enums;

/**
 * Represents the status of a payment transaction.
 */
public enum PaymentStatus {

    /** Payment has been initiated but not yet processed */
    INITIATED,

    /** Payment is currently being processed by the payment gateway */
    PROCESSING,

    /** Payment completed successfully */
    COMPLETED,

    /** Payment failed (declined, error, timeout) */
    FAILED,

    /** Payment has been fully refunded */
    REFUNDED,

    /** Payment has been partially refunded */
    PARTIALLY_REFUNDED
}
