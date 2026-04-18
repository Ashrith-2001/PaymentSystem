package com.payment.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Inventory entity — tracks stock levels for each product.
 * 
 * Key concurrency consideration:
 * The 'availableQuantity' field is the critical contention point.
 * Multiple concurrent orders could try to reserve the same stock.
 * We use PESSIMISTIC_WRITE locking in the repository to serialize access.
 */
@Entity
@Table(name = "inventory", indexes = {
        @Index(name = "idx_inventory_product", columnList = "productId", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Reference to the product (from Product Service) */
    @Column(nullable = false, unique = true)
    private Long productId;

    /** Total quantity in warehouse */
    @Column(nullable = false)
    private int totalQuantity;

    /** Quantity available for sale (total - reserved) */
    @Column(nullable = false)
    private int availableQuantity;

    /** Quantity currently reserved for in-progress orders */
    @Column(nullable = false)
    @Builder.Default
    private int reservedQuantity = 0;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Reserve stock for an order.
     * Decreases available quantity and increases reserved quantity.
     * 
     * @param quantity amount to reserve
     * @throws IllegalArgumentException if insufficient stock
     */
    public void reserveStock(int quantity) {
        if (availableQuantity < quantity) {
            throw new IllegalArgumentException(
                    String.format("Insufficient stock for product %d: requested %d, available %d",
                            productId, quantity, availableQuantity));
        }
        this.availableQuantity -= quantity;
        this.reservedQuantity += quantity;
    }

    /**
     * Release previously reserved stock (compensating transaction).
     * Increases available quantity and decreases reserved quantity.
     */
    public void releaseStock(int quantity) {
        this.availableQuantity += quantity;
        this.reservedQuantity -= quantity;
    }

    /**
     * Confirm reserved stock (after successful payment).
     * Decreases total and reserved quantities.
     */
    public void confirmReservation(int quantity) {
        this.totalQuantity -= quantity;
        this.reservedQuantity -= quantity;
    }
}
