package com.payment.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Event published when inventory state changes (reservation, release).
 * Consumed by: Order Service (to continue/compensate SAGA).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InventoryEvent extends BaseEvent {

    private Long orderId;
    private String reservationStatus; // "RESERVED", "RELEASED", "FAILED"
    private String failureReason;
    private List<InventoryItemEvent> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InventoryItemEvent {
        private Long productId;
        private int quantity;
        private boolean reserved;
    }

    /**
     * Factory: Inventory successfully reserved for an order.
     */
    public static InventoryEvent inventoryReserved(Long orderId, List<InventoryItemEvent> items) {
        InventoryEvent event = new InventoryEvent();
        event.initBaseEvent("INVENTORY_RESERVED", "inventory-service");
        event.setOrderId(orderId);
        event.setReservationStatus("RESERVED");
        event.setItems(items);
        return event;
    }

    /**
     * Factory: Inventory reservation failed (e.g., insufficient stock).
     */
    public static InventoryEvent inventoryFailed(Long orderId, String reason) {
        InventoryEvent event = new InventoryEvent();
        event.initBaseEvent("INVENTORY_FAILED", "inventory-service");
        event.setOrderId(orderId);
        event.setReservationStatus("FAILED");
        event.setFailureReason(reason);
        return event;
    }

    /**
     * Factory: Inventory released (compensating transaction).
     */
    public static InventoryEvent inventoryReleased(Long orderId) {
        InventoryEvent event = new InventoryEvent();
        event.initBaseEvent("INVENTORY_RELEASED", "inventory-service");
        event.setOrderId(orderId);
        event.setReservationStatus("RELEASED");
        return event;
    }
}
