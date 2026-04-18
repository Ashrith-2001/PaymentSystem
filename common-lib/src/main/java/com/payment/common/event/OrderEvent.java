package com.payment.common.event;

import com.payment.common.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Event published when an order state changes.
 * Consumed by: Payment Service, Inventory Service, Notification Service.
 * 
 * This is the primary event in the SAGA orchestration flow.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderEvent extends BaseEvent {

    private Long orderId;
    private Long userId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private List<OrderItemEvent> items;

    /** Reason for status change (especially useful for failures) */
    private String reason;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemEvent {
        private Long productId;
        private int quantity;
        private BigDecimal unitPrice;
    }

    /**
     * Factory method: Create an ORDER_CREATED event.
     */
    public static OrderEvent orderCreated(Long orderId, Long userId, BigDecimal totalAmount,
                                           List<OrderItemEvent> items) {
        OrderEvent event = new OrderEvent();
        event.initBaseEvent("ORDER_CREATED", "order-service");
        event.setOrderId(orderId);
        event.setUserId(userId);
        event.setTotalAmount(totalAmount);
        event.setItems(items);
        event.setStatus(OrderStatus.PENDING);
        return event;
    }

    /**
     * Factory method: Create an ORDER_CONFIRMED event.
     */
    public static OrderEvent orderConfirmed(Long orderId, Long userId) {
        OrderEvent event = new OrderEvent();
        event.initBaseEvent("ORDER_CONFIRMED", "order-service");
        event.setOrderId(orderId);
        event.setUserId(userId);
        event.setStatus(OrderStatus.CONFIRMED);
        return event;
    }

    /**
     * Factory method: Create an ORDER_CANCELLED event.
     */
    public static OrderEvent orderCancelled(Long orderId, Long userId, String reason) {
        OrderEvent event = new OrderEvent();
        event.initBaseEvent("ORDER_CANCELLED", "order-service");
        event.setOrderId(orderId);
        event.setUserId(userId);
        event.setStatus(OrderStatus.CANCELLED);
        event.setReason(reason);
        return event;
    }
}
