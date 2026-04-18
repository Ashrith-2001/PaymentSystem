package com.payment.inventory.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.common.config.KafkaTopics;
import com.payment.common.event.InventoryEvent;
import com.payment.common.event.OrderEvent;
import com.payment.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka integration for Inventory Service.
 * 
 * Listens for: ORDER_CREATED events → reserves stock
 * Publishes: INVENTORY_RESERVED / INVENTORY_FAILED events
 * 
 * This is part of the SAGA choreography:
 * Order Service → [ORDER_CREATED] → Inventory Service → [INVENTORY_RESERVED] → Payment Service
 */
@Component
@RequiredArgsConstructor
public class InventoryEventHandler {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventHandler.class);

    private final InventoryService inventoryService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Consume ORDER_CREATED events from Kafka.
     * Attempts to reserve stock for all items in the order.
     * Publishes INVENTORY_RESERVED on success, INVENTORY_FAILED on failure.
     */
    @KafkaListener(topics = KafkaTopics.ORDER_EVENTS, groupId = "inventory-service-group")
    public void handleOrderEvent(String message) {
        try {
            OrderEvent event = objectMapper.readValue(message, OrderEvent.class);

            if (!"ORDER_CREATED".equals(event.getEventType())) {
                log.debug("Ignoring non-ORDER_CREATED event: {}", event.getEventType());
                return;
            }

            log.info("Received ORDER_CREATED event for order {}", event.getOrderId());

            try {
                // Attempt stock reservation (with pessimistic locking)
                inventoryService.reserveStockForOrder(event.getOrderId(), event.getItems());

                // Publish success event
                InventoryEvent successEvent = InventoryEvent.inventoryReserved(
                        event.getOrderId(), null);
                publishEvent(KafkaTopics.INVENTORY_EVENTS, successEvent);

                log.info("Published INVENTORY_RESERVED for order {}", event.getOrderId());

            } catch (Exception ex) {
                log.error("Stock reservation failed for order {}: {}", event.getOrderId(), ex.getMessage());

                // Publish failure event (triggers SAGA compensation)
                InventoryEvent failEvent = InventoryEvent.inventoryFailed(
                        event.getOrderId(), ex.getMessage());
                publishEvent(KafkaTopics.INVENTORY_EVENTS, failEvent);
            }

        } catch (Exception ex) {
            log.error("Failed to process order event: {}", ex.getMessage(), ex);
        }
    }

    private void publishEvent(String topic, Object event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, json);
        } catch (Exception ex) {
            log.error("Failed to publish event to {}: {}", topic, ex.getMessage(), ex);
        }
    }
}
