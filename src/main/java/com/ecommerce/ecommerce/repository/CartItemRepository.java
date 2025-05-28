//package com.ecommerce.ecommerce.repository;
//
//import org.springframework.stereotype.Repository;
//import org.springframework.data.jpa.repository.JpaRepository;
//import com.ecommerce.ecommerce.entity.CartItem;
//
//import java.util.List;
//import java.util.Optional;
//import com.ecommerce.ecommerce.entity.ShoppingCart;
//import com.ecommerce.ecommerce.entity.Product;
//
//@Repository
//public interface CartItemRepository extends JpaRepository<CartItem, Long> {
//    List<CartItem> findByCart(ShoppingCart cart);
//    List<CartItem> findByCartId(Long cartId);
//    Optional<CartItem> findByCartAndProduct(ShoppingCart cart, Product product);
//    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
//}