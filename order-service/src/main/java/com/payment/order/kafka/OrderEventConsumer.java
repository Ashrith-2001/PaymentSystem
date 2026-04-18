package com.payment.order.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.common.config.KafkaTopics;
import com.payment.common.enums.OrderStatus;
import com.payment.common.event.InventoryEvent;
import com.payment.common.event.NotificationEvent;
import com.payment.common.event.OrderEvent;
import com.payment.common.event.PaymentEvent;
import com.payment.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for the Order Service — SAGA event handler.
 * 
 * Listens for events from other services and updates order status:
 * - INVENTORY_RESERVED → update to INVENTORY_RESERVED
 * - INVENTORY_FAILED → update to INVENTORY_FAILED (SAGA compensation)
 * - PAYMENT_COMPLETED → update to CONFIRMED
 * - PAYMENT_FAILED → update to PAYMENT_FAILED, trigger inventory release
 * 
 * This is the OBSERVER pattern applied to the SAGA flow —
 * the Order Service "observes" events from other services and reacts.
 */
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final OrderService orderService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Listen for inventory events (SAGA step 2 results).
     */
    @KafkaListener(topics = KafkaTopics.INVENTORY_EVENTS, groupId = "order-service-group")
    public void handleInventoryEvent(String message) {
        try {
            InventoryEvent event = objectMapper.readValue(message, InventoryEvent.class);
            log.info("Received {} event for order {}", event.getEventType(), event.getOrderId());

            switch (event.getReservationStatus()) {
                case "RESERVED":
                    // Inventory reserved → update order status
                    orderService.updateOrderStatus(event.getOrderId(),
                            OrderStatus.INVENTORY_RESERVED, "Inventory reserved successfully");
                    log.info("Order {} → INVENTORY_RESERVED", event.getOrderId());
                    break;

                case "FAILED":
                    // Inventory failed → mark order as failed (no compensation needed)
                    orderService.updateOrderStatus(event.getOrderId(),
                            OrderStatus.INVENTORY_FAILED,
                            "Inventory reservation failed: " + event.getFailureReason());
                    log.warn("Order {} → INVENTORY_FAILED: {}", event.getOrderId(), event.getFailureReason());
                    break;

                case "RELEASED":
                    log.info("Inventory released for order {} (compensation complete)", event.getOrderId());
                    break;

                default:
                    log.warn("Unknown inventory event status: {}", event.getReservationStatus());
            }
        } catch (Exception ex) {
            log.error("Failed to process inventory event: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Listen for payment events (SAGA step 3 results).
     */
    @KafkaListener(topics = KafkaTopics.PAYMENT_EVENTS, groupId = "order-service-group")
    public void handlePaymentEvent(String message) {
        try {
            PaymentEvent event = objectMapper.readValue(message, PaymentEvent.class);
            log.info("Received {} event for order {}", event.getEventType(), event.getOrderId());

            switch (event.getEventType()) {
                case "PAYMENT_COMPLETED":
                    // Payment successful → confirm order
                    orderService.updateOrderStatus(event.getOrderId(),
                            OrderStatus.CONFIRMED, "Payment completed. Transaction: " + event.getTransactionId());
                    log.info("Order {} → CONFIRMED (payment: {})", event.getOrderId(), event.getTransactionId());

                    // Publish notification event
                    publishNotification(event);

                    // Publish order confirmed event
                    OrderEvent confirmedEvent = OrderEvent.orderConfirmed(event.getOrderId(), event.getUserId());
                    publishEvent(KafkaTopics.ORDER_EVENTS, confirmedEvent);
                    break;

                case "PAYMENT_FAILED":
                    // Payment failed → mark order, trigger inventory release (compensation)
                    orderService.updateOrderStatus(event.getOrderId(),
                            OrderStatus.PAYMENT_FAILED,
                            "Payment failed: " + event.getFailureReason());
                    log.warn("Order {} → PAYMENT_FAILED. Triggering inventory release.", event.getOrderId());

                    // Publish ORDER_CANCELLED to trigger inventory release (compensating transaction)
                    OrderEvent cancelEvent = OrderEvent.orderCancelled(
                            event.getOrderId(), event.getUserId(),
                            "Payment failed: " + event.getFailureReason());
                    publishEvent(KafkaTopics.ORDER_EVENTS, cancelEvent);
                    break;

                default:
                    log.warn("Unknown payment event type: {}", event.getEventType());
            }
        } catch (Exception ex) {
            log.error("Failed to process payment event: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Publish a notification event for order confirmation.
     */
    private void publishNotification(PaymentEvent paymentEvent) {
        try {
            NotificationEvent notification = NotificationEvent.emailNotification(
                    paymentEvent.getUserId(),
                    null, // Email would be fetched from User Service in production
                    "Order Confirmed",
                    String.format("Your order has been confirmed. Payment of %s received via %s. Transaction ID: %s",
                            paymentEvent.getAmount(), paymentEvent.getPaymentMethod(), paymentEvent.getTransactionId()),
                    String.valueOf(paymentEvent.getOrderId()));

            publishEvent(KafkaTopics.NOTIFICATION_EVENTS, notification);
            log.info("Published notification for order confirmation to user {}", paymentEvent.getUserId());
        } catch (Exception ex) {
            log.error("Failed to publish notification event: {}", ex.getMessage(), ex);
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
