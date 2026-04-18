package com.payment.common.exception;

/**
 * Thrown when a payment cannot be processed due to insufficient funds.
 * Returns HTTP 402 Payment Required.
 */
public class InsufficientFundsException extends BusinessException {

    public InsufficientFundsException(String message) {
        super(message, "ERR_INSUFFICIENT_FUNDS");
    }
}
