package com.payment.common.exception;

/**
 * Thrown when a duplicate/repeated request is detected.
 * Used for idempotency enforcement in payment processing.
 * Returns HTTP 409 Conflict.
 */
public class DuplicateRequestException extends BusinessException {

    public DuplicateRequestException(String idempotencyKey) {
        super(
            String.format("Duplicate request detected with idempotency key: %s", idempotencyKey),
            "ERR_DUPLICATE_REQUEST"
        );
    }
}
