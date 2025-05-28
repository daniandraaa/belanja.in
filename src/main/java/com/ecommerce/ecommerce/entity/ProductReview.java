//package com.ecommerce.ecommerce.entity;
//
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.util.Date;
//
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//@Entity
//@Table(name = "product_reviews")
//public class ProductReview {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "rating")
//    private Integer rating;
//
//    @Column(name = "comment")
//    private String comment;
//
//    @Column(name = "review_date")
//    private Date reviewDate;
//
//    @ManyToOne
//    @JoinColumn(name = "product_id")
//    private Product product;
//
//    @ManyToOne
//    @JoinColumn(name = "buyer_id")
//    private Buyer buyer;
//
//    @PrePersist
//    protected void onCreate() {
//        reviewDate = new Date();
//    }
//}
