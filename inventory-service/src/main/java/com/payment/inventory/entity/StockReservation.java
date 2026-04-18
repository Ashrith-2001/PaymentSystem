package com.payment.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * StockReservation — tracks individual stock reservations per order.
 * Used to know exactly how much stock to release if an order fails.
 */
@Entity
@Table(name = "stock_reservations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class StockReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private int quantity;

    /** RESERVED, CONFIRMED, RELEASED */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "RESERVED";

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
