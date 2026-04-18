package com.payment.common.enums;

/**
 * Represents the lifecycle status of an order in the system.
 * Follows the SAGA orchestration flow:
 * PENDING → INVENTORY_RESERVED → PAYMENT_PROCESSING → CONFIRMED
 * 
 * Failure states trigger compensating transactions.
 */
public enum OrderStatus {

    /** Order has been created, awaiting processing */
    PENDING,

    /** Inventory has been successfully reserved for this order */
    INVENTORY_RESERVED,

    /** Inventory reservation failed (insufficient stock) */
    INVENTORY_FAILED,

    /** Payment is currently being processed */
    PAYMENT_PROCESSING,

    /** Payment completed successfully */
    PAYMENT_COMPLETED,

    /** Payment failed (insufficient funds, declined, etc.) */
    PAYMENT_FAILED,

    /** Order is fully confirmed and ready for fulfillment */
    CONFIRMED,

    /** Order has been cancelled (by user or system) */
    CANCELLED,

    /** Order has been refunded */
    REFUNDED,

    /** Order is being delivered */
    DELIVERING,

    /** Order has been delivered */
    DELIVERED
}
