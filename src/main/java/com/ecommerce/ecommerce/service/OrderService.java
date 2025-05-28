package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.entity.*;
import com.ecommerce.ecommerce.model.OrderDto;
import com.ecommerce.ecommerce.model.OrderItemDto;
import com.ecommerce.ecommerce.repository.OrderRepository;
import com.ecommerce.ecommerce.repository.ProductRepository;
import com.ecommerce.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public OrderDto createOrder(OrderDto orderDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = new Order();
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.PENDING);
        order.setShippingAddress(orderDto.getShippingAddress());
        order.setUser(user);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemDto itemDto : orderDto.getOrderItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            if (product.getStock() < itemDto.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItem.setOrder(order);

            orderItems.add(orderItem);

            // Calculate total
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            // Update product stock
            product.setStock(product.getStock() - itemDto.getQuantity());
            productRepository.save(product);
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);
        return mapToDto(savedOrder);
    }

    public List<OrderDto> getMyOrders() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return orderRepository.findByUserOrderByOrderDateDesc(user).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public OrderDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Check if current user owns this order
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        if (!order.getUser().getUsername().equals(currentUsername)) {
            throw new RuntimeException("You can only view your own orders");
        }

        return mapToDto(order);
    }

    private OrderDto mapToDto(Order order) {
        OrderDto orderDto = new OrderDto();
        orderDto.setId(order.getId());
        orderDto.setOrderDate(order.getOrderDate());
        orderDto.setStatus(order.getStatus());
        orderDto.setTotalAmount(order.getTotalAmount());
        orderDto.setShippingAddress(order.getShippingAddress());
        orderDto.setBuyerUsername(order.getUser().getUsername());
        orderDto.setBuyerName(order.getUser().getName());

        List<OrderItemDto> orderItemDtos = order.getOrderItems().stream()
                .map(this::mapOrderItemToDto)
                .collect(Collectors.toList());
        orderDto.setOrderItems(orderItemDtos);

        return orderDto;
    }

    private OrderItemDto mapOrderItemToDto(OrderItem orderItem) {
        OrderItemDto dto = new OrderItemDto();
        dto.setId(orderItem.getId());
        dto.setProductId(orderItem.getProduct().getId());
        dto.setProductName(orderItem.getProduct().getName());
        dto.setQuantity(orderItem.getQuantity());
        dto.setPrice(orderItem.getPrice());
        dto.setSubtotal(orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        dto.setStoreName(orderItem.getProduct().getStore().getName());
        return dto;
    }
}