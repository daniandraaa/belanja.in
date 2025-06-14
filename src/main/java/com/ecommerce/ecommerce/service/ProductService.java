package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.entity.*;
import com.ecommerce.ecommerce.model.ProductDto;
import com.ecommerce.ecommerce.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreRepository storeRepository;

    // @Autowired
    // private CategoryRepository categoryRepository; // Dikomentari karena tidak digunakan lagi

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;


    public ProductDto createProduct(ProductDto productDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Store store = storeRepository.findByOwner(user)
                .orElseThrow(() -> new RuntimeException("User doesn't have a store"));

        Product product = new Product();
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setStock(productDto.getStock());
        product.setImageUrl(productDto.getImageUrl());
        product.setStore(store);

        /* // Logika kategori dikomentari
        if (productDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }
        */

        Product savedProduct = productRepository.save(product);
        return mapToDto(savedProduct);
    }

    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return mapToDto(product);
    }

    public List<ProductDto> getProductsByStore(String storeUsername) {
        return productRepository.findByStoreUsernameOrderByIdDesc(storeUsername).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /* // Metode getProductsByCategory dikomentari
    public List<ProductDto> getProductsByCategory(Long categoryId) {
        // return productRepository.findByCategoryId(categoryId).stream() // Penggunaan findByCategoryId juga dikomentari
        //         .map(this::mapToDto)
        //         .collect(Collectors.toList());
        return java.util.Collections.emptyList(); // Kembalikan list kosong atau throw exception jika dipanggil
    }
    */

    public List<ProductDto> searchProducts(String name) {
        return productRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public ProductDto updateProduct(Long id, ProductDto productDto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Check if current user owns this product
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        if (!product.getStore().getOwner().getUsername().equals(currentUsername)) {
            throw new RuntimeException("You can only update your own products");
        }

        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setStock(productDto.getStock());
        product.setImageUrl(productDto.getImageUrl());

        /* // Logika kategori dikomentari
        if (productDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }
        */

        Product updatedProduct = productRepository.save(product);
        return mapToDto(updatedProduct);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Check if current user owns this product
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        if (!product.getStore().getOwner().getUsername().equals(currentUsername)) {
            throw new RuntimeException("You can only delete your own products");
        }

        productRepository.delete(product);
    }

    private ProductDto mapToDto(Product product) {
        ProductDto productDto = new ProductDto();
        productDto.setId(product.getId());
        productDto.setName(product.getName());
        productDto.setDescription(product.getDescription());
        productDto.setPrice(product.getPrice());
        productDto.setStock(product.getStock());
        productDto.setImageUrl(product.getImageUrl());
        if (product.getStore() != null) { // Tambahkan null check untuk store jika ada kemungkinan null
            productDto.setStoreId(product.getStore().getId());
            productDto.setStoreName(product.getStore().getName());
            productDto.setStoreUsername(product.getStore().getUsername()); // Pastikan getUsername() ada di Store atau User terkait
        }


        /* // Logika kategori di mapToDto dikomentari
        if (product.getCategory() != null) { // product.getCategory() akan error jika field category sudah dikomentari di Product.java
            // productDto.setCategoryId(product.getCategory().getId()); // Field ini sudah tidak ada di ProductDto
            // productDto.setCategoryName(product.getCategory().getName()); // Field ini sudah tidak ada di ProductDto
        }
        */

        productDto.setAverageRating(product.getAverageRating());
        productDto.setReviewCount(product.getReviewCount());

        return productDto;
    }

    @Transactional
    public void updateProductReviewStats(Long productId) {
        // Ambil produk dari database
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        // Rekomendasi: Gunakan custom exception ResourceNotFoundException

        // Ambil semua review yang terkait dengan produk ini
        List<Review> reviews = reviewRepository.findByProduct(product);

        if (reviews.isEmpty()) {
            // Jika tidak ada review, reset statistik ke nol
            product.setAverageRating(0.0);
            product.setReviewCount(0);
        } else {
            // Hitung rata-rata rating
            double average = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0); // Default ke 0 jika stream kosong (seharusnya tidak terjadi karena sudah dicek .isEmpty())

            product.setAverageRating(average);
            product.setReviewCount(reviews.size()); // Jumlah review adalah ukuran dari list
        }

        // Simpan perubahan statistik ke database
        productRepository.save(product);
    }
}