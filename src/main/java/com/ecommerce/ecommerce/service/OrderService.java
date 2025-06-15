package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.entity.*;
import com.ecommerce.ecommerce.model.OrderDto;
import com.ecommerce.ecommerce.model.OrderItemDto;
import com.ecommerce.ecommerce.model.PaymentDetailDto;
import com.ecommerce.ecommerce.model.UpdateOrderStatusDto;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;    @Autowired
    private CartRepository cartRepository;    @Autowired
    private OrderStatusSchedulerService statusSchedulerService;

    @Autowired
    private PaymentService paymentService;

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
            }            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setStore(product.getStore()); // Set store from product
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

    // Method untuk seller melihat pesanan produk dari toko mereka
    public List<OrderDto> getOrdersForMyStore() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User seller = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUsername));

        // Cek apakah user memiliki toko
        if (seller.getStore() == null) {
            throw new RuntimeException("You don't have a store");
        }

        List<Order> orders = orderRepository.findOrdersByStore(seller.getStore());
        return orders.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Method untuk mendapatkan detail order dengan validasi seller ownership
    public OrderDto getOrderByIdForSeller(Long orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User seller = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUsername));

        if (seller.getStore() == null) {
            throw new RuntimeException("You don't have a store");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Validasi bahwa order mengandung produk dari toko seller
        boolean hasSellerItems = order.getOrderItems().stream()
                .anyMatch(item -> item.getStore().getId().equals(seller.getStore().getId()));

        if (!hasSellerItems) {
            throw new org.springframework.security.access.AccessDeniedException("This order doesn't contain products from your store");
        }        return mapToDto(order);
    }    private OrderDto mapToDto(Order order) {
        OrderDto orderDto = new OrderDto();
        orderDto.setId(order.getId());
        orderDto.setOrderDate(order.getOrderDate());
        orderDto.setStatusUpdatedAt(order.getStatusUpdatedAt());
        orderDto.setStatus(order.getStatus());
        orderDto.setTotalAmount(order.getTotalAmount());
        orderDto.setShippingAddress(order.getShippingAddress());
        orderDto.setBuyerUsername(order.getUser().getUsername());
        orderDto.setBuyerName(order.getUser().getName());
        orderDto.setPaymentDetail(paymentService.mapToDto(order.getPaymentDetail()));

        List<OrderItemDto> orderItemDtos = order.getOrderItems().stream()
                .map(this::mapOrderItemToDto)
                .collect(Collectors.toList());
        orderDto.setOrderItems(orderItemDtos);

        return orderDto;
    }private OrderItemDto mapOrderItemToDto(OrderItem orderItem) {
        OrderItemDto dto = new OrderItemDto();
        dto.setId(orderItem.getId());
        dto.setProductId(orderItem.getProduct().getId());
        dto.setProductName(orderItem.getProduct().getName());
        dto.setQuantity(orderItem.getQuantity());
        dto.setPrice(orderItem.getPrice());
        dto.setSubtotal(orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        // Use store from OrderItem for better performance
        dto.setStoreId(orderItem.getStore().getId());
        dto.setStoreName(orderItem.getStore().getName());
        dto.setStoreUsername(orderItem.getStore().getUsername());
        return dto;
    }    @Transactional // **SANGAT PENTING**
    public OrderDto createOrderFromCurrentUserCart(String shippingAddress, PaymentDetailDto paymentDetailDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUsername));

        // Menggunakan CartRepository yang di-inject untuk mendapatkan Cart
        Cart cart = cartRepository.findByUser(currentUser)
                .orElseThrow(() -> new RuntimeException("Shopping cart is empty or not found for user: " + currentUsername));        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cannot create order from an empty cart.");
        }

        // Validate and create payment detail
        PaymentDetail paymentDetail = paymentService.createPaymentDetail(paymentDetailDto);

        Order order = new Order();        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.PENDING);
        order.setStatusUpdatedAt(LocalDateTime.now());
        order.setShippingAddress(shippingAddress);
        order.setUser(currentUser);
        order.setPaymentDetail(paymentDetail);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal calculatedTotalAmount = BigDecimal.ZERO;

        for (com.ecommerce.ecommerce.entity.CartItem cartItem : new ArrayList<>(cart.getItems())) { // Iterasi pada salinan untuk menghindari ConcurrentModificationException jika 'cart.getItems().clear()' memodifikasi list asli saat masih diiterasi
            Product product = cartItem.getProduct();

            if (product.getStock() == null || product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName() +
                        ". Available: " + (product.getStock() == null ? 0 : product.getStock()) +
                        ", Requested: " + cartItem.getQuantity());
            }            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setStore(product.getStore()); // Set store from product
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


    @Transactional
    public OrderDto createDirectOrder(Long productId, int quantity, String shippingAddress, PaymentDetailDto paymentDetailDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUsername));


        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));


        if (product.getStock() == null || product.getStock() < quantity) {
            throw new RuntimeException("Insufficient stock for product: " + product.getName() +
                    ". Available: " + (product.getStock() == null ? 0 : product.getStock()) +
                    ", Requested: " + quantity);
        }


        PaymentDetail paymentDetail = paymentService.createPaymentDetail(paymentDetailDto);


        Order order = new Order();
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.PENDING);
        order.setStatusUpdatedAt(LocalDateTime.now());
        order.setShippingAddress(shippingAddress);
        order.setUser(currentUser);
        order.setPaymentDetail(paymentDetail);


        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setStore(product.getStore()); // Set store from product
        orderItem.setQuantity(quantity);
        orderItem.setPrice(product.getPrice());
        orderItem.setOrder(order);

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItem);
        order.setOrderItems(orderItems);


        BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        order.setTotalAmount(totalAmount);


        product.setStock(product.getStock() - quantity);
        productRepository.save(product);


        Order savedOrder = orderRepository.save(order);

        return mapToDto(savedOrder);
    }

    // Helper method to restore product stock
    private void restoreProductStock(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }
    }    // Helper method to validate seller ownership of all items in an order
    private void validateSellerOwnsOrderItems(Order order, String sellerUsername) {
        User seller = userRepository.findByUsername(sellerUsername)
                .orElseThrow(() -> new RuntimeException("Seller not found: " + sellerUsername));

        if (seller.getStore() == null) {
            throw new RuntimeException("Seller doesn't have a store");
        }

        for (OrderItem item : order.getOrderItems()) {
            // Use store from OrderItem for better performance
            if (!item.getStore().getId().equals(seller.getStore().getId())) {
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

    // Method untuk seller update status order
    @Transactional
    public OrderDto updateOrderStatusBySeller(Long orderId, UpdateOrderStatusDto updateDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User seller = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUsername));

        if (seller.getStore() == null) {
            throw new RuntimeException("You don't have a store");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Validasi bahwa order mengandung produk dari toko seller
        boolean hasSellerItems = order.getOrderItems().stream()
                .anyMatch(item -> item.getStore().getId().equals(seller.getStore().getId()));

        if (!hasSellerItems) {
            throw new org.springframework.security.access.AccessDeniedException("This order doesn't contain products from your store");
        }        // Validasi status transition
        validateStatusTransition(order.getStatus(), updateDto.getStatus());

        order.setStatus(updateDto.getStatus());
        order.setStatusUpdatedAt(LocalDateTime.now());
        Order updatedOrder = orderRepository.save(order);
        return mapToDto(updatedOrder);
    }    // Method untuk validasi transisi status yang valid
    private void validateStatusTransition(Order.OrderStatus currentStatus, Order.OrderStatus newStatus) {
        // Define valid status transitions
        switch (currentStatus) {
            case PENDING:
                if (newStatus != Order.OrderStatus.PROCESSING && newStatus != Order.OrderStatus.CANCELLED) {
                    throw new IllegalStateException("Order status can only be changed from PENDING to PROCESSING or CANCELLED");
                }
                break;
            case PROCESSING:
                if (newStatus != Order.OrderStatus.SHIPPED && newStatus != Order.OrderStatus.CANCELLED) {
                    throw new IllegalStateException("Order status can only be changed from PROCESSING to SHIPPED or CANCELLED");
                }
                break;
            case SHIPPED:
                if (newStatus != Order.OrderStatus.DELIVERED) {
                    throw new IllegalStateException("Order status can only be changed from SHIPPED to DELIVERED");
                }
                break;
            case DELIVERED:
                if (newStatus != Order.OrderStatus.ACCEPTED) {
                    throw new IllegalStateException("Order status can only be changed from DELIVERED to ACCEPTED");
                }
                break;
            case ACCEPTED:
                throw new IllegalStateException("Cannot change status of an accepted order");
            case CANCELLED:
                throw new IllegalStateException("Cannot change status of a cancelled order");
            default:
                throw new IllegalStateException("Unknown order status: " + currentStatus);
        }
    }// Method untuk seller accept order (auto processing -> shipped -> delivered)
    @Transactional
    public OrderDto acceptOrderBySeller(Long orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User seller = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUsername));

        if (seller.getStore() == null) {
            throw new RuntimeException("You don't have a store");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Validasi ownership
        boolean hasSellerItems = order.getOrderItems().stream()
                .anyMatch(item -> item.getStore().getId().equals(seller.getStore().getId()));

        if (!hasSellerItems) {
            throw new org.springframework.security.access.AccessDeniedException("This order doesn't contain products from your store");
        }

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new IllegalStateException("Can only accept orders with PENDING status. Current status: " + order.getStatus());
        }

        // Update to PROCESSING and set timestamp
        order.setStatus(Order.OrderStatus.PROCESSING);
        order.setStatusUpdatedAt(LocalDateTime.now());
        Order updatedOrder = orderRepository.save(order);

        // Start async status updates (PROCESSING -> SHIPPED -> DELIVERED)
        statusSchedulerService.scheduleStatusUpdate(orderId);

        return mapToDto(updatedOrder);
    }

    // Method untuk mendapatkan status tracking order
    public Map<String, Object> getOrderStatusTracking(Long orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUsername));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Check if user is owner or seller of any items in the order
        boolean isOwner = order.getUser().getId().equals(user.getId());
        boolean isSeller = user.getStore() != null &&
                order.getOrderItems().stream()
                        .anyMatch(item -> item.getStore().getId().equals(user.getStore().getId()));

        if (!isOwner && !isSeller) {
            throw new org.springframework.security.access.AccessDeniedException("You don't have access to this order");
        }

        Map<String, Object> tracking = new HashMap<>();
        tracking.put("orderId", order.getId());
        tracking.put("currentStatus", order.getStatus());
        tracking.put("orderDate", order.getOrderDate());
        // Status timeline
        List<Map<String, Object>> timeline = new ArrayList<>();
        timeline.add(Map.of(
                "status", "PENDING",
                "completed", order.getStatus() != Order.OrderStatus.PENDING,
                "description", "Order placed and waiting for seller confirmation"
        ));
        timeline.add(Map.of(
                "status", "PROCESSING",
                "completed", order.getStatus() == Order.OrderStatus.PROCESSING ||
                        order.getStatus() == Order.OrderStatus.SHIPPED ||
                        order.getStatus() == Order.OrderStatus.DELIVERED ||
                        order.getStatus() == Order.OrderStatus.ACCEPTED,
                "description", "Order confirmed and being prepared"
        ));
        timeline.add(Map.of(
                "status", "SHIPPED",
                "completed", order.getStatus() == Order.OrderStatus.SHIPPED ||
                        order.getStatus() == Order.OrderStatus.DELIVERED ||
                        order.getStatus() == Order.OrderStatus.ACCEPTED,
                "description", "Order shipped and on the way"
        ));
        timeline.add(Map.of(
                "status", "DELIVERED",
                "completed", order.getStatus() == Order.OrderStatus.DELIVERED ||
                        order.getStatus() == Order.OrderStatus.ACCEPTED,
                "description", "Order delivered successfully"
        ));
        timeline.add(Map.of(
                "status", "ACCEPTED",
                "completed", order.getStatus() == Order.OrderStatus.ACCEPTED,
                "description", "Order accepted by buyer - can now leave reviews"
        ));

        tracking.put("timeline", timeline);
        tracking.put("canCancel", order.getStatus() == Order.OrderStatus.PENDING);
        tracking.put("canUpdate", isSeller && order.getStatus() != Order.OrderStatus.DELIVERED &&
                order.getStatus() != Order.OrderStatus.ACCEPTED &&
                order.getStatus() != Order.OrderStatus.CANCELLED);
        tracking.put("canAccept", isOwner && order.getStatus() == Order.OrderStatus.DELIVERED);
        tracking.put("canReview", isOwner && order.getStatus() == Order.OrderStatus.ACCEPTED);

        return tracking;
    }

    // Method untuk buyer accept order (setelah delivered)
    @Transactional
    public OrderDto acceptOrderByBuyer(Long orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User buyer = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUsername));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Validasi bahwa user adalah buyer dari order ini
        if (!order.getUser().getId().equals(buyer.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("You can only accept your own orders");
        }

        if (order.getStatus() != Order.OrderStatus.DELIVERED) {
            throw new IllegalStateException("Can only accept orders with DELIVERED status. Current status: " + order.getStatus());
        }

        order.setStatus(Order.OrderStatus.ACCEPTED);
        order.setStatusUpdatedAt(LocalDateTime.now());
        Order updatedOrder = orderRepository.save(order);

        return mapToDto(updatedOrder);
    }

}