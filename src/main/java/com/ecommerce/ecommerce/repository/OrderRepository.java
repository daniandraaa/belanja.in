package com.ecommerce.ecommerce.repository;

import com.ecommerce.ecommerce.entity.Order;
import com.ecommerce.ecommerce.entity.Store;
import com.ecommerce.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    List<Order> findByUserOrderByOrderDateDesc(User user);

    // Query untuk mendapatkan orders yang mengandung produk dari store tertentu
    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi WHERE oi.store = :store ORDER BY o.orderDate DESC")
    List<Order> findOrdersByStore(@Param("store") Store store);

    // Query untuk mendapatkan orders yang mengandung produk dari store tertentu dan user tertentu
    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi WHERE oi.store = :store AND o.user = :user ORDER BY o.orderDate DESC")
    List<Order> findOrdersByStoreAndUser(@Param("store") Store store, @Param("user") User user);
}