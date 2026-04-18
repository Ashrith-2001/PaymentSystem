package com.payment.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base event class for all Kafka events in the system.
 * Provides common fields: eventId, eventType, timestamp, and source.
 * All domain events should extend this class.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent implements Serializable {

    /** Unique identifier for this event (for idempotency and tracing) */
    private String eventId;

    /** Type of the event (e.g., "ORDER_CREATED", "PAYMENT_COMPLETED") */
    private String eventType;

    /** When the event was created */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    /** Source service that produced this event */
    private String source;

    /**
     * Initialize base event fields. Called by subclass constructors.
     */
    protected void initBaseEvent(String eventType, String source) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
        this.source = source;
    }
}
