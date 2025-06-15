package com.ecommerce.ecommerce.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemDto {
    private Long id;

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;    // Response fields
    private String productName;
    private BigDecimal price;
    private BigDecimal subtotal;
    private Long storeId;
    private String storeName;
    private String storeUsername;
}