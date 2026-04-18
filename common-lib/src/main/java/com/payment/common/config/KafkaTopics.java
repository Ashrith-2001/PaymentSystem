package com.payment.common.config;

/**
 * Centralized Kafka topic names used across all microservices.
 * Ensures consistency — both producers and consumers reference the same constants.
 */
public final class KafkaTopics {

    private KafkaTopics() {
        // Prevent instantiation — constants only
    }

    /** Order lifecycle events (created, confirmed, cancelled) */
    public static final String ORDER_EVENTS = "order-events";

    /** Payment lifecycle events (completed, failed, refunded) */
    public static final String PAYMENT_EVENTS = "payment-events";

    /** Inventory events (reserved, released, failed) */
    public static final String INVENTORY_EVENTS = "inventory-events";

    /** Notification events (email, SMS triggers) */
    public static final String NOTIFICATION_EVENTS = "notification-events";
}
