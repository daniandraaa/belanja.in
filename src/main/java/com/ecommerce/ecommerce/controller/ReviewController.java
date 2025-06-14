package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.model.CreateReviewRequestDto;
import com.ecommerce.ecommerce.model.ReviewResponseDto;
import com.ecommerce.ecommerce.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api") // Menggunakan base path /api
@CrossOrigin(origins = "*")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /**
     * Endpoint untuk membuat review baru untuk sebuah produk.
     * Pengguna harus sudah membeli produk ini dan memberikan ID OrderItem yang relevan.
     */
    @PostMapping("/products/{productId}/reviews")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')") // Hanya pengguna dengan peran BUYER atau SELLER yang bisa membuat review.
    public ResponseEntity<ReviewResponseDto> createProductReview(
            @PathVariable Long productId,
            @Valid @RequestBody CreateReviewRequestDto reviewRequestDto) {

        ReviewResponseDto createdReview = reviewService.createReview(productId, reviewRequestDto);
        return new ResponseEntity<>(createdReview, HttpStatus.CREATED);
    }

    /**
     * Endpoint untuk mendapatkan semua review dari sebuah produk (dengan paginasi).
     * Endpoint ini bersifat publik dan tidak memerlukan autentikasi.
     */
    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<Page<ReviewResponseDto>> getProductReviews(
            @PathVariable Long productId,
            @PageableDefault(size = 10, sort = "reviewDate", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ReviewResponseDto> reviews = reviewService.getReviewsForProduct(productId, pageable);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Endpoint opsional untuk menghapus sebuah review.
     * Hanya pemilik review atau admin yang bisa menghapus.
     */
    @DeleteMapping("/reviews/{reviewId}")
    @PreAuthorize("hasAnyRole('BUYER', 'ADMIN')") // Asumsi Admin juga bisa menghapus review.
    public ResponseEntity<Void> deleteProductReview(@PathVariable Long reviewId) {
        // Logika validasi kepemilikan atau peran admin ada di dalam service.
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}