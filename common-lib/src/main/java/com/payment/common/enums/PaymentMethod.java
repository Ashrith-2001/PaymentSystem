package com.payment.common.enums;

/**
 * Supported payment methods in the system.
 * Each method maps to a specific PaymentStrategy implementation
 * via the Strategy pattern.
 */
public enum PaymentMethod {

    CREDIT_CARD,
    DEBIT_CARD,
    NET_BANKING,
    WALLET,
    UPI
}
