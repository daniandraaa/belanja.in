package com.ecommerce.ecommerce.model;

import com.ecommerce.ecommerce.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PaymentDetailDto {

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    // Card fields (required only for CARD payment)
    @Size(min = 2, max = 100, message = "Card holder name must be between 2 and 100 characters")
    private String cardHolderName;

    @Pattern(regexp = "^[0-9]{13,19}$", message = "Card number must be 13-19 digits")
    private String cardNumber;

    @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV must be 3-4 digits")
    private String cvv;

    @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{4}$", message = "Expiry date must be in MM/YYYY format")
    private String expiryDate;
}
