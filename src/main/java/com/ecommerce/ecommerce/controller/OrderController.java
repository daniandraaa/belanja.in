package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.model.CheckoutRequestDto;
import com.ecommerce.ecommerce.model.DirectCheckoutRequestDto;
import com.ecommerce.ecommerce.model.OrderDto;
import com.ecommerce.ecommerce.model.UpdateOrderStatusDto;
import com.ecommerce.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
            return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    public ResponseEntity<List<OrderDto>> getMyOrders() {
        List<OrderDto> orders = orderService.getMyOrders();
        return ResponseEntity.ok(orders);
    }

    // Endpoint baru untuk seller melihat pesanan produk dari toko mereka
    @GetMapping("/my-store-orders")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<List<OrderDto>> getMyStoreOrders() {
        try {
            List<OrderDto> orders = orderService.getOrdersForMyStore();
            return ResponseEntity.ok(orders);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }    @PostMapping("/checkout/from-cart")
    @PreAuthorize("hasRole('BUYER')") // Hanya BUYER yang bisa checkout dari keranjangnya
    public ResponseEntity<?> checkoutFromCart(@Valid @RequestBody CheckoutRequestDto checkoutRequestDto) {
        try {
            // Memanggil metode baru di OrderService dengan payment detail
            OrderDto createdOrder = orderService.createOrderFromCurrentUserCart(
                    checkoutRequestDto.getShippingAddress(),
                    checkoutRequestDto.getPaymentDetail()
            );
            return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now()
            ));
        }
    }

    // Endpoint untuk direct checkout (beli langsung tanpa cart)
    @PostMapping("/checkout/direct")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<?> directCheckout(@Valid @RequestBody DirectCheckoutRequestDto directCheckoutDto) {
        try {
            OrderDto createdOrder = orderService.createDirectOrder(
                    directCheckoutDto.getProductId(),
                    directCheckoutDto.getQuantity(),
                    directCheckoutDto.getShippingAddress(),
                    directCheckoutDto.getPaymentDetail()
            );
            return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now()
            ));
        }
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



    // Endpoint untuk seller update status order
    @PutMapping("/{orderId}/update-status")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusDto updateDto) {
        try {
            OrderDto updatedOrder = orderService.updateOrderStatusBySeller(orderId, updateDto);
            return ResponseEntity.ok(updatedOrder);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now()
            ));
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now()
            ));
        }
    }    // Endpoint untuk seller confirm dan ship order sekaligus
    @PutMapping("/{orderId}/confirm-and-ship")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> confirmAndShipOrder(@PathVariable Long orderId) {
        try {
            OrderDto updatedOrder = orderService.acceptOrderBySeller(orderId);
            return ResponseEntity.ok(updatedOrder);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now()
            ));
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now()
            ));
        }
    }

    // Endpoint untuk seller accept order (auto-process)
    @PutMapping("/{orderId}/accept")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> acceptOrderBySeller(@PathVariable Long orderId) {
        try {
            OrderDto updatedOrder = orderService.acceptOrderBySeller(orderId);
            return ResponseEntity.ok(Map.of(
                    "message", "Order accepted and will be automatically processed",
                    "order", updatedOrder,
                    "note", "Status will automatically change: PROCESSING (15s) -> SHIPPED (15s) -> DELIVERED",
                    "timestamp", java.time.LocalDateTime.now()
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now()
            ));
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now()
            ));
        }
    }

    // Endpoint untuk buyer accept order setelah delivered
    @PutMapping("/{orderId}/accept-delivery")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<?> acceptOrderByBuyer(@PathVariable Long orderId) {
        try {
            OrderDto updatedOrder = orderService.acceptOrderByBuyer(orderId);
            return ResponseEntity.ok(Map.of(
                    "message", "Order accepted! You can now leave reviews for the products",
                    "order", updatedOrder,
                    "timestamp", java.time.LocalDateTime.now()
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now()
            ));
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now()
            ));
        }
    }

    // Endpoint untuk tracking status order
    @GetMapping("/{orderId}/tracking")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    public ResponseEntity<?> getOrderTracking(@PathVariable Long orderId) {
        try {
            Map<String, Object> tracking = orderService.getOrderStatusTracking(orderId);
            return ResponseEntity.ok(tracking);
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now()
            ));
        }
    }
}