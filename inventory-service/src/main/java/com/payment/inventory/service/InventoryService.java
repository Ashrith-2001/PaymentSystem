package com.payment.inventory.service;

import com.payment.common.event.InventoryEvent;
import com.payment.common.event.OrderEvent;
import com.payment.common.exception.InsufficientStockException;
import com.payment.common.exception.ResourceNotFoundException;
import com.payment.inventory.dto.InventoryDTO;
import com.payment.inventory.entity.Inventory;
import com.payment.inventory.entity.StockReservation;
import com.payment.inventory.repository.InventoryRepository;
import com.payment.inventory.repository.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Inventory Service — Core stock management business logic.
 * 
 * Concurrency Safety:
 * - All stock-modifying operations use pessimistic locking via findByProductIdForUpdate()
 * - This ensures that concurrent orders cannot oversell stock
 * - The lock is held for the duration of the transaction
 * 
 * SAGA Integration:
 * - reserveStockForOrder(): Called when ORDER_CREATED event is received
 * - releaseStockForOrder(): Compensating transaction when payment fails
 * - confirmStockForOrder(): Called when order is confirmed
 */
@Service
@RequiredArgsConstructor
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final InventoryRepository inventoryRepository;
    private final StockReservationRepository stockReservationRepository;

    /**
     * Reserve stock for an order (SAGA step).
     * Uses pessimistic locking to prevent overselling.
     * 
     * @param orderId the order requesting the reservation
     * @param items list of product-quantity pairs to reserve
     * @throws InsufficientStockException if any product has insufficient stock
     */
    @Transactional
    public void reserveStockForOrder(Long orderId, List<OrderEvent.OrderItemEvent> items) {
        log.info("Reserving stock for order {}", orderId);

        List<InventoryEvent.InventoryItemEvent> reservedItems = new ArrayList<>();

        for (OrderEvent.OrderItemEvent item : items) {
            // PESSIMISTIC_WRITE lock — blocks concurrent access to same product
            Inventory inventory = inventoryRepository.findByProductIdForUpdate(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", item.getProductId()));

            // Check and reserve stock (domain method handles validation)
            if (inventory.getAvailableQuantity() < item.getQuantity()) {
                // Rollback all previous reservations in this transaction
                throw new InsufficientStockException(item.getProductId(), item.getQuantity(),
                        inventory.getAvailableQuantity());
            }

            inventory.reserveStock(item.getQuantity());
            inventoryRepository.save(inventory);

            // Track the reservation for potential compensation later
            StockReservation reservation = StockReservation.builder()
                    .orderId(orderId)
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .status("RESERVED")
                    .build();
            stockReservationRepository.save(reservation);

            reservedItems.add(InventoryEvent.InventoryItemEvent.builder()
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .reserved(true)
                    .build());

            log.debug("Reserved {} units of product {} for order {}",
                    item.getQuantity(), item.getProductId(), orderId);
        }

        log.info("Stock reserved successfully for order {} ({} items)", orderId, items.size());
    }

    /**
     * Release reserved stock (compensating transaction).
     * Called when payment fails or order is cancelled.
     */
    @Transactional
    public void releaseStockForOrder(Long orderId) {
        log.info("Releasing stock for order {} (compensating transaction)", orderId);

        List<StockReservation> reservations = stockReservationRepository
                .findByOrderIdAndStatus(orderId, "RESERVED");

        for (StockReservation reservation : reservations) {
            Inventory inventory = inventoryRepository.findByProductIdForUpdate(reservation.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", reservation.getProductId()));

            inventory.releaseStock(reservation.getQuantity());
            inventoryRepository.save(inventory);

            reservation.setStatus("RELEASED");
            stockReservationRepository.save(reservation);

            log.debug("Released {} units of product {} for order {}",
                    reservation.getQuantity(), reservation.getProductId(), orderId);
        }

        log.info("Stock released for order {}", orderId);
    }

    /**
     * Confirm reserved stock (after successful payment).
     */
    @Transactional
    public void confirmStockForOrder(Long orderId) {
        log.info("Confirming stock for order {}", orderId);

        List<StockReservation> reservations = stockReservationRepository
                .findByOrderIdAndStatus(orderId, "RESERVED");

        for (StockReservation reservation : reservations) {
            Inventory inventory = inventoryRepository.findByProductIdForUpdate(reservation.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", reservation.getProductId()));

            inventory.confirmReservation(reservation.getQuantity());
            inventoryRepository.save(inventory);

            reservation.setStatus("CONFIRMED");
            stockReservationRepository.save(reservation);
        }

        log.info("Stock confirmed for order {}", orderId);
    }

    // ==================== CRUD Operations ====================

    @Transactional
    public InventoryDTO addInventory(InventoryDTO dto) {
        log.info("Adding inventory for product {}", dto.getProductId());

        Inventory inventory = Inventory.builder()
                .productId(dto.getProductId())
                .totalQuantity(dto.getTotalQuantity())
                .availableQuantity(dto.getTotalQuantity())
                .reservedQuantity(0)
                .build();

        Inventory saved = inventoryRepository.save(inventory);
        return mapToDTO(saved);
    }

    @Transactional(readOnly = true)
    public InventoryDTO getInventoryByProductId(Long productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", productId));
        return mapToDTO(inventory);
    }

    @Transactional
    public InventoryDTO updateStock(Long productId, int additionalQuantity) {
        Inventory inventory = inventoryRepository.findByProductIdForUpdate(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", productId));

        inventory.setTotalQuantity(inventory.getTotalQuantity() + additionalQuantity);
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + additionalQuantity);

        Inventory updated = inventoryRepository.save(inventory);
        log.info("Stock updated for product {}: +{}", productId, additionalQuantity);
        return mapToDTO(updated);
    }

    private InventoryDTO mapToDTO(Inventory inventory) {
        return InventoryDTO.builder()
                .id(inventory.getId())
                .productId(inventory.getProductId())
                .totalQuantity(inventory.getTotalQuantity())
                .availableQuantity(inventory.getAvailableQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .build();
    }
}
