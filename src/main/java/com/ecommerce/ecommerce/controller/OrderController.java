package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.model.CheckoutRequestDto;
import com.ecommerce.ecommerce.model.OrderDto;
import com.ecommerce.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody OrderDto orderDto) {
        try {
            OrderDto createdOrder = orderService.createOrder(orderDto);
            return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    public ResponseEntity<List<OrderDto>> getMyOrders() {
        List<OrderDto> orders = orderService.getMyOrders();
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/checkout/from-cart")
    @PreAuthorize("hasRole('BUYER')") // Hanya BUYER yang bisa checkout dari keranjangnya
    public ResponseEntity<OrderDto> checkoutFromCart(@Valid @RequestBody CheckoutRequestDto checkoutRequestDto) {
        // Memanggil metode baru di OrderService yang telah kita rancang
        OrderDto createdOrder = orderService.createOrderFromCurrentUserCart(checkoutRequestDto.getShippingAddress());
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        try {
            OrderDto orderDto = orderService.getOrderById(id);
            return ResponseEntity.ok(orderDto);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{orderId}/cancel-by-buyer")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<OrderDto> cancelOrderByBuyer(@PathVariable Long orderId) {
        try {
            OrderDto updatedOrder = orderService.cancelOrderAsBuyer(orderId);
            return ResponseEntity.ok(updatedOrder);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Or a custom error DTO
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // For OrderNotFound or UserNotFound
        }
    }

    @PutMapping("/{orderId}/cancel-by-seller")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<OrderDto> cancelOrderBySeller(@PathVariable Long orderId) {
        try {
            OrderDto updatedOrder = orderService.cancelOrderAsSeller(orderId);
            return ResponseEntity.ok(updatedOrder);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PutMapping("/{orderId}/process-and-ship")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<OrderDto> processAndShipOrder(@PathVariable Long orderId) {
        try {
            OrderDto updatedOrder = orderService.processAndShipOrderAsSeller(orderId);
            return ResponseEntity.ok(updatedOrder);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PutMapping("/{orderId}/mark-delivered")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')") // Buyer or Seller (acting as buyer for their own purchase)
    public ResponseEntity<OrderDto> markOrderAsDelivered(@PathVariable Long orderId) {
        try {
            OrderDto updatedOrder = orderService.markOrderAsDelivered(orderId);
            return ResponseEntity.ok(updatedOrder);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Or a custom error DTO
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (RuntimeException e) {
            // Catching generic RuntimeException for cases like OrderNotFound or UserNotFound
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}