package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.model.AddItemRequestDto;
import com.ecommerce.ecommerce.model.CartViewDto;
import com.ecommerce.ecommerce.model.UpdateItemQuantityRequestDto;
import com.ecommerce.ecommerce.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Pastikan ini diimpor
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*") // Sesuaikan jika perlu untuk lingkungan produksi
@PreAuthorize("hasAnyRole('BUYER', 'SELLER')") // Melindungi semua endpoint di controller ini untuk peran BUYER atau SELLER
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public ResponseEntity<CartViewDto> getCurrentUserCart() {
        return ResponseEntity.ok(cartService.getCartForCurrentUser());
    }

    @PostMapping("/items")
    public ResponseEntity<CartViewDto> addItemToCart(@Valid @RequestBody AddItemRequestDto addItemRequestDto) {
        return ResponseEntity.ok(cartService.addItemToCart(addItemRequestDto));
    }

    // Di sini, {productId} adalah ID dari Produk, bukan CartItem.
    // Jika Anda ingin menggunakan ID CartItem, maka DTO dan service perlu disesuaikan.
    @PutMapping("/items/product/{productId}")
    public ResponseEntity<CartViewDto> updateCartItemQuantity(@PathVariable Long productId,
                                                              @Valid @RequestBody UpdateItemQuantityRequestDto updateDto) {
        return ResponseEntity.ok(cartService.updateItemQuantity(productId, updateDto));
    }

    @DeleteMapping("/items/product/{productId}")
    public ResponseEntity<CartViewDto> removeCartItem(@PathVariable Long productId) {
        return ResponseEntity.ok(cartService.removeItemFromCart(productId));
    }

    @DeleteMapping("/clear") // Endpoint yang lebih spesifik untuk mengosongkan keranjang
    public ResponseEntity<Void> clearCurrentUserCart() {
        cartService.clearCartForCurrentUser();
        return ResponseEntity.noContent().build();
    }
}