package com.payment.payment.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.common.config.KafkaTopics;
import com.payment.common.event.InventoryEvent;
import com.payment.common.event.PaymentEvent;
import com.payment.payment.dto.PaymentRequest;
import com.payment.payment.dto.PaymentResponse;
import com.payment.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka event handler for Payment Service.
 * 
 * Listens for: INVENTORY_RESERVED → triggers payment processing
 * Publishes: PAYMENT_COMPLETED / PAYMENT_FAILED → consumed by Order Service
 * 
 * SAGA flow position:
 * Inventory Service → [INVENTORY_RESERVED] → Payment Service → [PAYMENT_COMPLETED] → Order Service
 */
@Component
@RequiredArgsConstructor
public class PaymentEventHandler {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventHandler.class);

    private final PaymentService paymentService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Consume INVENTORY_RESERVED events.
     * When inventory is successfully reserved, process the payment.
     */
    @KafkaListener(topics = KafkaTopics.INVENTORY_EVENTS, groupId = "payment-service-group")
    public void handleInventoryEvent(String message) {
        try {
            InventoryEvent event = objectMapper.readValue(message, InventoryEvent.class);

            if (!"INVENTORY_RESERVED".equals(event.getEventType())) {
                log.debug("Ignoring non-INVENTORY_RESERVED event: {}", event.getEventType());
                return;
            }

            log.info("Received INVENTORY_RESERVED for order {}", event.getOrderId());

            // Note: In a real system, we'd fetch order details to get userId, amount, paymentMethod
            // For this demo, we'll publish a payment event that the Order Service handles
            // The actual payment processing happens when the Order Service calls our REST API
            
            log.info("Payment Service ready to process payment for order {}. " +
                     "Awaiting payment request via REST API or orchestrator.", event.getOrderId());

        } catch (Exception ex) {
            log.error("Failed to process inventory event: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Publish a payment event to Kafka.
     * Called by PaymentService after processing a payment.
     */
    public void publishPaymentEvent(PaymentResponse response) {
        try {
            PaymentEvent event;
            if ("COMPLETED".equals(response.getStatus().name())) {
                event = PaymentEvent.paymentCompleted(
                        response.getId(), response.getOrderId(), response.getUserId(),
                        response.getAmount(), response.getPaymentMethod(),
                        response.getTransactionId());
            } else {
                event = PaymentEvent.paymentFailed(
                        response.getId(), response.getOrderId(), response.getUserId(),
                        response.getAmount(), response.getFailureReason());
            }

            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaTopics.PAYMENT_EVENTS, json);
            log.info("Published {} event for order {}", event.getEventType(), response.getOrderId());

        } catch (Exception ex) {
            log.error("Failed to publish payment event: {}", ex.getMessage(), ex);
        }
    }
}
