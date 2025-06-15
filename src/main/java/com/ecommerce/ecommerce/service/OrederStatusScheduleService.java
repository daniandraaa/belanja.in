package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.entity.Order;
import com.ecommerce.ecommerce.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Service
public class OrederStatusScheduleService {

    @Autowired
    private OrderRepository orderRepository;

    @Async
    @Transactional
    public CompletableFuture<Void> scheduleStatusUpdate(Long orderId) {
        try {
            // Wait 15 seconds then update to SHIPPED
            Thread.sleep(15000);
            updateOrderStatus(orderId, Order.OrderStatus.SHIPPED);

            // Wait another 15 seconds then update to DELIVERED
            Thread.sleep(15000);
            updateOrderStatus(orderId, Order.OrderStatus.DELIVERED);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // Handle interruption
        }

        return CompletableFuture.completedFuture(null);
    }

    private void updateOrderStatus(Long orderId, Order.OrderStatus newStatus) {
        orderRepository.findById(orderId).ifPresent(order -> {
            // Only update if current status allows it
            if (canUpdateToStatus(order.getStatus(), newStatus)) {
                order.setStatus(newStatus);
                order.setStatusUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
            }
        });
    }

    private boolean canUpdateToStatus(Order.OrderStatus currentStatus, Order.OrderStatus newStatus) {
        switch (currentStatus) {
            case PROCESSING:
                return newStatus == Order.OrderStatus.SHIPPED;
            case SHIPPED:
                return newStatus == Order.OrderStatus.DELIVERED;
            default:
                return false;
        }
    }
}
