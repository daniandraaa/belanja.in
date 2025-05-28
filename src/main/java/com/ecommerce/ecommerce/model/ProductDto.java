package com.ecommerce.ecommerce.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDto {
    private Long id;

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @NotNull(message = "Stock is required")
    @Positive(message = "Stock must be positive")
    private Integer stock;

    private String imageUrl;

    // For request
    private Long categoryId;

    // For response
    private String categoryName;
    private Long storeId;
    private String storeName;
    private String storeUsername;
}