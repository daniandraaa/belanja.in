package com.ecommerce.ecommerce.controller;

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
}