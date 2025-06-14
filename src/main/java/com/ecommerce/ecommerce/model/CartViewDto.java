package com.ecommerce.ecommerce.model; // atau sub-package com.ecommerce.ecommerce.model.cart

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartViewDto {
    private Long cartId;
    private List<CartItemDto> items;
    private BigDecimal totalAmount;
    private Integer totalItemsCount; // Jumlah total semua item (sum of quantities)
}