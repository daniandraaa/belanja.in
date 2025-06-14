package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.entity.*;
import com.ecommerce.ecommerce.model.OrderDto;
import com.ecommerce.ecommerce.model.OrderItemDto;
import com.ecommerce.ecommerce.repository.OrderRepository;
import com.ecommerce.ecommerce.repository.ProductRepository;
import com.ecommerce.ecommerce.repository.UserRepository;
import com.ecommerce.ecommerce.repository.CartRepository;
import jakarta.transaction.Transactional;
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

    @Autowired
    private CartRepository cartRepository;

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

    @Transactional // **SANGAT PENTING**
    public OrderDto createOrderFromCurrentUserCart(String shippingAddress) { // Atau DTO Checkout
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUsername));

        // Menggunakan CartRepository yang di-inject untuk mendapatkan Cart
        Cart cart = cartRepository.findByUser(currentUser)
                .orElseThrow(() -> new RuntimeException("Shopping cart is empty or not found for user: " + currentUsername));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cannot create order from an empty cart.");
        }

        Order order = new Order();
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.PENDING);
        order.setShippingAddress(shippingAddress);
        order.setUser(currentUser);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal calculatedTotalAmount = BigDecimal.ZERO;

        for (com.ecommerce.ecommerce.entity.CartItem cartItem : new ArrayList<>(cart.getItems())) { // Iterasi pada salinan untuk menghindari ConcurrentModificationException jika 'cart.getItems().clear()' memodifikasi list asli saat masih diiterasi
            Product product = cartItem.getProduct();

            if (product.getStock() == null || product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName() +
                        ". Available: " + (product.getStock() == null ? 0 : product.getStock()) +
                        ", Requested: " + cartItem.getQuantity());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItem.setOrder(order);
            orderItems.add(orderItem);

            // Anda mungkin sudah punya metode getSubtotal di OrderItem, jika tidak:
            // calculatedTotalAmount = calculatedTotalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            // Jika OrderItem.getSubtotal() ada dan benar:
            calculatedTotalAmount = calculatedTotalAmount.add(orderItem.getSubtotal());


            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(calculatedTotalAmount);

        Order savedOrder = orderRepository.save(order);

        // Kosongkan keranjang (items-nya saja)
        cart.getItems().clear(); // Hapus semua item dari koleksi
        cartRepository.save(cart); // Simpan cart yang sudah kosong (orphanRemoval akan menghapus CartItems dari DB)

        return mapToDto(savedOrder); // Metode mapToDto Anda yang ada di OrderService
    }

    // Helper method to restore product stock
    private void restoreProductStock(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }
    }

    // Helper method to validate seller ownership of all items in an order
    private void validateSellerOwnsOrderItems(Order order, String sellerUsername) {
        User seller = userRepository.findByUsername(sellerUsername)
                .orElseThrow(() -> new RuntimeException("Seller not found: " + sellerUsername));

        for (OrderItem item : order.getOrderItems()) {
            // Updated to use getOwner() from Store entity
            if (item.getProduct().getStore() == null || item.getProduct().getStore().getOwner() == null ||
                    !item.getProduct().getStore().getOwner().getId().equals(seller.getId())) {
                throw new org.springframework.security.access.AccessDeniedException("Seller does not own all products in this order.");
            }
        }
    }

    @Transactional
    public OrderDto cancelOrderAsBuyer(Long orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User buyer = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUsername));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        if (!order.getUser().getId().equals(buyer.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Buyer can only cancel their own orders.");
        }

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new IllegalStateException("Order cannot be cancelled by buyer as it is already " + order.getStatus());
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        restoreProductStock(order);
        Order updatedOrder = orderRepository.save(order);
        return mapToDto(updatedOrder);
    }

    @Transactional
    public OrderDto cancelOrderAsSeller(Long orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        validateSellerOwnsOrderItems(order, currentUsername);

        if (order.getStatus() == Order.OrderStatus.SHIPPED || order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new IllegalStateException("Order cannot be cancelled by seller as it is already " + order.getStatus());
        }
        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order is already cancelled.");
        }


        order.setStatus(Order.OrderStatus.CANCELLED);
        restoreProductStock(order);
        Order updatedOrder = orderRepository.save(order);
        return mapToDto(updatedOrder);
    }

    @Transactional
    public OrderDto processAndShipOrderAsSeller(Long orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        validateSellerOwnsOrderItems(order, currentUsername);

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new IllegalStateException("Order can only be processed and shipped if its status is PENDING. Current status: " + order.getStatus());
        }

        order.setStatus(Order.OrderStatus.SHIPPED);
        // Stock was already reduced when the order was created. No need to change stock here.
        Order updatedOrder = orderRepository.save(order);
        return mapToDto(updatedOrder);
    }

    @Transactional
    public OrderDto markOrderAsDelivered(Long orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUsername));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        // Validate that the current user is the buyer of this order
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("You can only mark your own orders as delivered.");
        }

        // Validate that the order status is SHIPPED
        if (order.getStatus() != Order.OrderStatus.SHIPPED) {
            throw new IllegalStateException("Order can only be marked as delivered if its status is SHIPPED. Current status: " + order.getStatus());
        }

        order.setStatus(Order.OrderStatus.DELIVERED);
        Order updatedOrder = orderRepository.save(order);
        return mapToDto(updatedOrder);
    }

}