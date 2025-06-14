package com.ecommerce.ecommerce.model;

import com.ecommerce.ecommerce.entity.Order;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDto {
    private Long id;

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    @NotEmpty(message = "Order items cannot be empty")
    private List<OrderItemDto> orderItems;

    // Response fields
    private LocalDateTime orderDate;
    private Order.OrderStatus status;
    private BigDecimal totalAmount;
    private String buyerUsername;
    private String buyerName;
}