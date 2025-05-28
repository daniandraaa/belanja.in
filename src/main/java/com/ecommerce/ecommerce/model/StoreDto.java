package com.ecommerce.ecommerce.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StoreDto {
    private Long id;

    @NotBlank(message = "Store username is required")
    @Size(min = 3, max = 50, message = "Store username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Store name is required")
    @Size(min = 3, max = 100, message = "Store name must be between 3 and 100 characters")
    private String name;

    @NotBlank(message = "Store address is required")
    private String address;

    // PASTIKAN FIELD INI ADA
    private String description;

    // Response fields
    private String ownerUsername;
    private String ownerName;
    private Integer productCount;
}