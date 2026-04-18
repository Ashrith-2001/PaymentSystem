package com.payment.order.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.common.config.KafkaTopics;
import com.payment.common.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka producer for order events.
 * Publishes events to the ORDER_EVENTS topic.
 */
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Publish an order event to Kafka.
     * Uses the orderId as the message key for partition ordering.
     */
    public void publishOrderEvent(OrderEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaTopics.ORDER_EVENTS, String.valueOf(event.getOrderId()), json);
            log.info("Published {} event for order {}", event.getEventType(), event.getOrderId());
        } catch (Exception ex) {
            log.error("Failed to publish order event for order {}: {}",
                    event.getOrderId(), ex.getMessage(), ex);
            // In production: use Transactional Outbox pattern to guarantee delivery
        }
    }
}
