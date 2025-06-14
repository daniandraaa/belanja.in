package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.entity.*;
// Jika Anda membuat custom exception:
// import com.ecommerce.ecommerce.exception.ResourceNotFoundException;
// import com.ecommerce.ecommerce.exception.BadRequestException;
import com.ecommerce.ecommerce.model.AddItemRequestDto;
import com.ecommerce.ecommerce.model.CartItemDto;
import com.ecommerce.ecommerce.model.CartViewDto;
import com.ecommerce.ecommerce.model.UpdateItemQuantityRequestDto;
import com.ecommerce.ecommerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // **SANGAT DIREKOMENDASIKAN**

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository; // Seperti di OrderService Anda

    @Autowired
    private UserRepository userRepository; // Seperti di service Anda yang lain

    // Helper untuk mendapatkan User yang sedang login (konsisten dengan service Anda)
    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        return userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUsername));
        // Rekomendasi: .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUsername));
    }

    // Helper untuk mendapatkan atau membuat Cart untuk user saat ini
    // Metode ini akan membuat Cart jika belum ada untuk user yang login.
    private Cart getOrCreateCartEntityForCurrentUser() {
        User currentUser = getCurrentAuthenticatedUser();
        return cartRepository.findByUser(currentUser).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(currentUser);
            return cartRepository.save(newCart); // Simpan cart baru
        });
    }

    // Mapper dari Cart entity ke CartViewDto (mengikuti pola Anda)
    private CartViewDto mapCartEntityToCartViewDto(Cart cart) {
        // Penting: Pastikan cart.getItems() terisi.
        // Jika LAZY, ini bisa menyebabkan masalah jika tidak dalam sesi transaksional
        // atau jika Cart diambil tanpa item-itemnya.
        // Menggunakan cartRepository.findById(cart.getId()) dapat membantu me-refresh.
        Cart refreshedCart = cartRepository.findById(cart.getId()).orElse(cart);

        List<CartItemDto> itemDtos = refreshedCart.getItems().stream().map(cartItem -> {
            Product product = cartItem.getProduct(); // Asumsi product di CartItem di-load
            return new CartItemDto(
                    cartItem.getId(), // ID dari CartItem
                    product.getId(),
                    product.getName(),
                    product.getImageUrl(),
                    cartItem.getQuantity(),
                    product.getPrice(),
                    cartItem.getSubtotal() // Menggunakan helper method dari CartItem
            );
        }).collect(Collectors.toList());

        int totalItemsCount = itemDtos.stream().mapToInt(CartItemDto::getQuantity).sum();
        BigDecimal totalAmount = refreshedCart.getTotalAmount(); // Menggunakan helper method dari Cart

        return new CartViewDto(
                refreshedCart.getId(),
                itemDtos,
                totalAmount,
                totalItemsCount
        );
    }

    // **Rekomendasi: Tambahkan @Transactional pada semua metode yang mengubah data**
    @Transactional
    public CartViewDto addItemToCart(AddItemRequestDto addItemRequestDto) {
        Cart cart = getOrCreateCartEntityForCurrentUser();
        Product product = productRepository.findById(addItemRequestDto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + addItemRequestDto.getProductId()));
        // Rekomendasi: .orElseThrow(() -> new ResourceNotFoundException("Product not found..."));

        if (product.getStock() == null || product.getStock() < addItemRequestDto.getQuantity()) {
            throw new RuntimeException("Not enough stock for product: " + product.getName());
            // Rekomendasi: throw new BadRequestException("Not enough stock...");
        }

        Optional<CartItem> existingCartItemOpt = cartItemRepository.findByCartAndProduct(cart, product);

        CartItem cartItem;
        if (existingCartItemOpt.isPresent()) {
            cartItem = existingCartItemOpt.get();
            int newQuantity = cartItem.getQuantity() + addItemRequestDto.getQuantity();
            if (product.getStock() < newQuantity) {
                throw new RuntimeException("Not enough stock to add more " + product.getName() + ". Requested total: " + newQuantity);
                // Rekomendasi: throw new BadRequestException("Not enough stock...");
            }
            cartItem.setQuantity(newQuantity);
        } else {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(addItemRequestDto.getQuantity());
            cart.getItems().add(cartItem); // Tambahkan ke koleksi di Cart juga
        }
        cartItemRepository.save(cartItem); // Simpan CartItem (baru atau terupdate)
        // Simpan cart untuk memperbarui 'updatedAt' dan jika ada perubahan pada koleksi 'items'
        // (tergantung konfigurasi cascade, tapi aman untuk save eksplisit)
        Cart updatedCart = cartRepository.save(cart);
        return mapCartEntityToCartViewDto(updatedCart);
    }

    @Transactional(readOnly = true) // Baik untuk metode yang hanya membaca
    public CartViewDto getCartForCurrentUser() {
        Cart cart = getOrCreateCartEntityForCurrentUser();
        return mapCartEntityToCartViewDto(cart);
    }

    @Transactional
    public CartViewDto updateItemQuantity(Long productIdInCart, UpdateItemQuantityRequestDto updateDto) {
        // Catatan: productIdInCart di sini mungkin lebih baik jika itu adalah cartItemId
        // agar lebih presisi. Jika menggunakan productId, pastikan unik per cart.
        Cart cart = getOrCreateCartEntityForCurrentUser();
        Product product = productRepository.findById(productIdInCart) // Asumsi productIdInCart adalah Product.id
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productIdInCart));

        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new RuntimeException("Product not in cart: " + product.getName()));
        // Rekomendasi: .orElseThrow(() -> new ResourceNotFoundException("Product not in cart..."));

        if (updateDto.getQuantity() <= 0) { // Jika kuantitas 0 atau negatif, hapus item
            cart.getItems().remove(cartItem); // Hapus dari koleksi di cart
            cartItemRepository.delete(cartItem); // Hapus dari DB
        } else {
            if (product.getStock() == null || product.getStock() < updateDto.getQuantity()) {
                throw new RuntimeException("Not enough stock for product: " + product.getName());
                // Rekomendasi: throw new BadRequestException("Not enough stock...");
            }
            cartItem.setQuantity(updateDto.getQuantity());
            cartItemRepository.save(cartItem);
        }
        Cart updatedCart = cartRepository.save(cart);
        return mapCartEntityToCartViewDto(updatedCart);
    }

    @Transactional
    public CartViewDto removeItemFromCart(Long productIdToRemove) {
        // Sama seperti update, ini adalah Product.id, bukan CartItem.id
        Cart cart = getOrCreateCartEntityForCurrentUser();
        Product product = productRepository.findById(productIdToRemove)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productIdToRemove));

        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new RuntimeException("Product not in cart: " + product.getName()));
        // Rekomendasi: .orElseThrow(() -> new ResourceNotFoundException("Product not in cart..."));

        cart.getItems().remove(cartItem);
        cartItemRepository.delete(cartItem);

        Cart updatedCart = cartRepository.save(cart);
        return mapCartEntityToCartViewDto(updatedCart);
    }

    @Transactional
    public void clearCartForCurrentUser() {
        Cart cart = getOrCreateCartEntityForCurrentUser();
        // Dengan orphanRemoval=true di Cart.items, ini akan menghapus CartItems dari DB
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}