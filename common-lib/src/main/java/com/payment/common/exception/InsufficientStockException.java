package com.payment.common.exception;

/**
 * Thrown when an inventory operation fails due to insufficient stock.
 * Returns HTTP 409 Conflict (resource state conflict).
 */
public class InsufficientStockException extends BusinessException {

    public InsufficientStockException(String message) {
        super(message, "ERR_INSUFFICIENT_STOCK");
    }

    public InsufficientStockException(Long productId, int requested, int available) {
        super(
            String.format("Insufficient stock for product %d: requested %d, available %d",
                productId, requested, available),
            "ERR_INSUFFICIENT_STOCK"
        );
    }
}
