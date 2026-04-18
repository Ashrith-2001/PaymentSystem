package com.payment.order.entity;

import com.payment.common.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * OrderStatusHistory — audit trail for order status changes.
 * Every status transition is recorded with timestamp and reason.
 * Useful for debugging SAGA flow and customer support.
 */
@Entity
@Table(name = "order_status_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus toStatus;

    @Column(length = 500)
    private String reason;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime changedAt;
}
