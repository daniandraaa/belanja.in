package com.ecommerce.ecommerce.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode; // Untuk menghindari masalah dengan Set/List jika ada circular dependency di toString/equals
import lombok.NoArgsConstructor;
import lombok.ToString; // Untuk menghindari masalah dengan Set/List

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal; // Pastikan import ini ada

@Entity
@Table(name = "carts")
@Data
@NoArgsConstructor
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @ToString.Exclude // Mencegah rekursi jika User juga punya referensi ke Cart
    @EqualsAndHashCode.Exclude
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<CartItem> items = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Catatan: Helper method seperti getTotalAmount() bisa juga diletakkan di service
    // atau saat mapping ke DTO untuk memisahkan logic dari entitas.
    // Jika tetap di sini, pastikan items sudah di-load jika LAZY.
    public BigDecimal getTotalAmount() {
        if (items == null) {
            return BigDecimal.ZERO;
        }
        return items.stream()
                .map(CartItem::getSubtotal) // Memanggil getSubtotal dari CartItem
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}