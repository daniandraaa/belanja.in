//package com.ecommerce.ecommerce.controller;
//
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import com.ecommerce.ecommerce.model.CartItemRequest;
//import com.ecommerce.ecommerce.model.CartResponse;
//import com.ecommerce.ecommerce.model.WebResponse;
//import com.ecommerce.ecommerce.service.CartService;
//
//import jakarta.validation.Valid;
//
//@RestController
//@RequestMapping("/api/cart")
//@PreAuthorize("hasRole('BUYER')")
//public class CartController {
//
//    @Autowired
//    private CartService cartService;
//
//    @GetMapping
//    public ResponseEntity<WebResponse<CartResponse>> getCart() {
//        WebResponse<CartResponse> response = cartService.getCart();
//        return ResponseEntity.ok(response);
//    }
//
//    @PostMapping("/items")
//    public ResponseEntity<WebResponse<CartResponse>> addToCart(@Valid @RequestBody CartItemRequest request) {
//        WebResponse<CartResponse> response = cartService.addToCart(request);
//        return ResponseEntity.ok(response);
//    }
//
//    @PutMapping("/items/{itemId}")
//    public ResponseEntity<WebResponse<CartResponse>> updateCartItem(
//            @PathVariable Long itemId,
//            @Valid @RequestBody CartItemRequest request) {
//        WebResponse<CartResponse> response = cartService.updateCartItem(itemId, request);
//        return ResponseEntity.ok(response);
//    }
//
//    @DeleteMapping("/items/{itemId}")
//    public ResponseEntity<WebResponse<CartResponse>> removeFromCart(@PathVariable Long itemId) {
//        WebResponse<CartResponse> response = cartService.removeFromCart(itemId);
//        return ResponseEntity.ok(response);
//    }
//
//    @DeleteMapping("/clear")
//    public ResponseEntity<WebResponse<String>> clearCart() {
//        WebResponse<String> response = cartService.clearCart();
//        return ResponseEntity.ok(response);
//    }
//}
