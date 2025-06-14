package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.entity.*;
// Jika Anda menggunakan custom exceptions:
// import com.ecommerce.ecommerce.exception.BadRequestException;
// import com.ecommerce.ecommerce.exception.ResourceNotFoundException;
// import com.ecommerce.ecommerce.exception.UnauthorizedOperationException;
import com.ecommerce.ecommerce.model.CreateReviewRequestDto;
import com.ecommerce.ecommerce.model.ReviewResponseDto;
import com.ecommerce.ecommerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderItemRepository orderItemRepository; // Diperlukan untuk validasi

    // Helper untuk mendapatkan User yang sedang login
    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        return userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUsername));
    }

    // Mapper dari Review entity ke ReviewResponseDto
    private ReviewResponseDto mapEntityToDto(Review review) {
        return new ReviewResponseDto(
                review.getId(),
                review.getRating(),
                review.getComment(),
                review.getReviewDate(),
                review.getUser().getUsername(),
                review.getProduct().getId(),
                review.getProduct().getName()
        );
    }

    @Transactional
    public ReviewResponseDto createReview(Long targetProductId, CreateReviewRequestDto reviewDto) {
        User currentUser = getCurrentAuthenticatedUser();

        Product product = productRepository.findById(targetProductId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + targetProductId));
        // Rekomendasi: .orElseThrow(() -> new ResourceNotFoundException(...));

        OrderItem orderItem = orderItemRepository.findById(reviewDto.getOrderItemId())
                .orElseThrow(() -> new RuntimeException("Order item not found with id: " + reviewDto.getOrderItemId()));
        // Rekomendasi: .orElseThrow(() -> new ResourceNotFoundException(...));

        // Validasi 1: Apakah OrderItem ini milik user yang sedang login?
        if (!orderItem.getOrder().getUser().equals(currentUser)) {
            throw new RuntimeException("This order item does not belong to the current user.");
            // Rekomendasi: throw new UnauthorizedOperationException(...);
        }

        // Validasi 2: Apakah OrderItem ini untuk produk yang dimaksud?
        if (!orderItem.getProduct().equals(product)) {
            throw new RuntimeException("This order item is not for the specified product.");
            // Rekomendasi: throw new BadRequestException(...);
        }

        // Validasi 3: Apakah order untuk OrderItem ini sudah selesai (misal, DELIVERED)?
        if (orderItem.getOrder().getStatus() != Order.OrderStatus.DELIVERED) { // Sesuaikan dengan status "selesai" Anda
            throw new RuntimeException("Product from this order item has not been delivered yet or order is not completed.");
            // Rekomendasi: throw new BadRequestException(...);
        }

        // Validasi 4: Apakah user sudah pernah mereview OrderItem ini?
        if (reviewRepository.existsByOrderItemAndUser(orderItem, currentUser)) {
            throw new RuntimeException("You have already reviewed this purchased item.");
            // Rekomendasi: throw new BadRequestException("Review already submitted for this purchase.");
        }

        Review review = new Review();
        review.setUser(currentUser);
        review.setProduct(product);
        review.setOrderItem(orderItem); // Kaitkan review dengan OrderItem spesifik
        review.setRating(reviewDto.getRating());
        review.setComment(reviewDto.getComment());

        Review savedReview = reviewRepository.save(review);

        // (Opsional) Update rata-rata rating di produk setelah review baru ditambahkan
        updateProductAverageRating(product.getId());

        return mapEntityToDto(savedReview);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponseDto> getReviewsForProduct(Long productId, Pageable pageable) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        return reviewRepository.findByProduct(product, pageable).map(this::mapEntityToDto);
    }

    // Opsional: Metode untuk menghapus review (perlu validasi pemilik atau admin)
    @Transactional
    public void deleteReview(Long reviewId) {
        User currentUser = getCurrentAuthenticatedUser();
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));

        // Validasi apakah user adalah pemilik review
        if (!review.getUser().equals(currentUser)) {
            // Anda bisa tambahkan pengecekan apakah user adalah ADMIN di sini
            // if(!currentUser.getRoles().stream().anyMatch(role -> role.getName() == RoleName.ADMIN)) {
            throw new RuntimeException("You are not authorized to delete this review.");
            // }
        }
        reviewRepository.delete(review);
        // (Opsional) Update rata-rata rating produk setelah review dihapus
        updateProductAverageRating(review.getProduct().getId());
    }

    // Opsional: Metode untuk memperbarui rata-rata rating produk
    // Ini bisa dipanggil setelah create, update, atau delete review
    @Transactional
    public void updateProductAverageRating(Long productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return;

        List<Review> reviews = reviewRepository.findByProduct(product);
        if (reviews.isEmpty()) {
            product.setAverageRating(0.0); // Atau null
            product.setReviewCount(0);
        } else {
            double average = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
            product.setAverageRating(average); // Pastikan tipe field di Product sesuai (Double)
            product.setReviewCount(reviews.size());
        }
        productRepository.save(product);
    }
}