package com.ecommerce.ecommerce.repository;

import com.ecommerce.ecommerce.entity.Cart;
import com.ecommerce.ecommerce.entity.CartItem;
import com.ecommerce.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
    List<CartItem> findByCartId(Long cartId); // Berguna untuk mengambil semua item dari sebuah keranjang
    void deleteByCartAndProduct(Cart cart, Product product); // Untuk menghapus item tertentu
}