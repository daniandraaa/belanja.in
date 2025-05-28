//package com.ecommerce.ecommerce.entity;
//
//
//import jakarta.persistence.*;
//import jakarta.validation.constraints.Min;
//import jakarta.validation.constraints.NotNull;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//@Entity
//@Table(name = "cart_items")
//public class CartItem {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @NotNull
//    @Min(value = 1, message = "Quantity must be at least 1")
//    private Integer quantity;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "cart_id", nullable = false)
//    private ShoppingCart cart;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "product_id", nullable = false)
//    private Product product;
//}