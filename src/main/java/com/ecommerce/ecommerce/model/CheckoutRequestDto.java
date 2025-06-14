package com.ecommerce.ecommerce.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor; // Opsional, jika Anda ingin konstruktor tanpa argumen
import lombok.AllArgsConstructor; // Opsional, jika Anda ingin konstruktor dengan semua argumen

@Data
@NoArgsConstructor  // Lombok akan membuat konstruktor tanpa argumen
@AllArgsConstructor // Lombok akan membuat konstruktor dengan semua argumen
public class CheckoutRequestDto {

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    // Anda bisa menambahkan field lain di sini di masa depan jika diperlukan, misalnya:
    // private String paymentMethodId;
    // private String notes;
}