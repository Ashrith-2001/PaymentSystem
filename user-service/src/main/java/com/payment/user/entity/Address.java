package com.payment.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Address entity — represents a shipping/billing address for a user.
 * A user can have multiple addresses with one marked as primary.
 */
@Entity
@Table(name = "addresses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String street;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(nullable = false, length = 10)
    private String zipCode;

    @Column(nullable = false, length = 100)
    private String country;

    /** Whether this is the user's primary/default address */
    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private boolean primary = false;

    /**
     * Many addresses belong to one user.
     * FetchType.LAZY: Don't load User when loading an Address (avoids N+1).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
