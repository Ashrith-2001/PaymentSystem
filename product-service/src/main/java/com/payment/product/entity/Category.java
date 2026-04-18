package com.payment.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Category entity — organizes products into logical groups.
 */
@Entity
@Table(name = "categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude  // Prevent circular toString with Product
    private List<Product> products = new ArrayList<>();
}
