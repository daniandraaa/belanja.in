package com.ecommerce.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    // Card payment fields (null for COD)
    private String cardHolderName;

    @Column(length = 20)
    private String cardNumber;

    @Column(length = 4)
    private String cvv;

    @Column(length = 7) // Format: MM/YYYY
    private String expiryDate;

    // One-to-one relationship with Order
    @OneToOne(mappedBy = "paymentDetail")
    private Order order;
}
