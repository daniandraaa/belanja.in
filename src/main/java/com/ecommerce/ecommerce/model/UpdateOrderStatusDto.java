package com.ecommerce.ecommerce.model;

import com.ecommerce.ecommerce.entity.Order;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrderStatusDto {

    @NotNull(message = "Status is required")
    private Order.OrderStatus status;

    private String notes; // Optional notes for status update
}
