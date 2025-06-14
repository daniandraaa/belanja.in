package com.ecommerce.ecommerce.repository;

import com.ecommerce.ecommerce.entity.Product;
import com.ecommerce.ecommerce.entity.Review;
import com.ecommerce.ecommerce.entity.User;
import com.ecommerce.ecommerce.entity.OrderItem; // Import OrderItem
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByProduct(Product product, Pageable pageable);
    List<Review> findByProduct(Product product); // Untuk kalkulasi rata-rata jika perlu semua

    // Untuk memeriksa apakah user sudah mereview OrderItem tertentu
    boolean existsByOrderItemAndUser(OrderItem orderItem, User user);
    Optional<Review> findByOrderItemAndUser(OrderItem orderItem, User user);



    // Untuk memeriksa apakah user sudah mereview suatu produk (jika tidak dikaitkan ke OrderItem spesifik)
    // boolean existsByProductAndUser(Product product, User user);
}