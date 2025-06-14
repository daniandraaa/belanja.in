package com.ecommerce.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "average_rating", columnDefinition = "DECIMAL(3,2) DEFAULT 0.0") // DECIMAL(3,2) untuk presisi misal 4.75
    private Double averageRating = 0.0;

    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "category_id")
    // private Category category; // Dikomentari


}