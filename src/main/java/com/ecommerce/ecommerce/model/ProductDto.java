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

    private String storeUsername; // Anda sudah punya ini

    // Field baru untuk statistik ulasan
    private Double averageRating;
    private Integer reviewCount;

    // For request
    // private Long categoryId; // Dihapus

    // For response
    // private String categoryName; // Dihapus
    private Long storeId;
    private String storeName;


}