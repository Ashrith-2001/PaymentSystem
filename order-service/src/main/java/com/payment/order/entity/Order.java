package com.payment.order.entity;

import com.payment.common.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order entity — represents a customer order.
 * 
 * Optimistic Locking:
 * The @Version field prevents lost updates when multiple requests
 * try to modify the same order concurrently. If two transactions
 * read the same version and both try to update, the second one
 * will fail with OptimisticLockException (and can be retried).
 */
@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_user", columnList = "userId"),
        @Index(name = "idx_orders_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 500)
    private String shippingAddress;

    @Column(length = 500)
    private String notes;

    /**
     * Optimistic Locking version field.
     * JPA automatically increments this on every update.
     * If two transactions read version=1 and both try to save,
     * the second one will get OptimisticLockException.
     */
    @Version
    private Long version;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Add an item to this order and set the back-reference.
     */
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    /**
     * Update order status and record it in the status history.
     */
    public void updateStatus(OrderStatus newStatus, String reason) {
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(this)
                .fromStatus(this.status)
                .toStatus(newStatus)
                .reason(reason)
                .build();
        this.statusHistory.add(history);
        this.status = newStatus;
    }
}
