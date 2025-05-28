package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.entity.Category;
import com.ecommerce.ecommerce.entity.Product;
import com.ecommerce.ecommerce.entity.Store;
import com.ecommerce.ecommerce.entity.User;
import com.ecommerce.ecommerce.model.ProductDto;
import com.ecommerce.ecommerce.repository.CategoryRepository;
import com.ecommerce.ecommerce.repository.ProductRepository;
import com.ecommerce.ecommerce.repository.StoreRepository;
import com.ecommerce.ecommerce.repository.UserRepository;
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

    @Autowired
    private CategoryRepository categoryRepository;

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

        if (productDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

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

    public List<ProductDto> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

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

        if (productDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

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
        productDto.setStoreId(product.getStore().getId());
        productDto.setStoreName(product.getStore().getName());
        productDto.setStoreUsername(product.getStore().getUsername());

        if (product.getCategory() != null) {
            productDto.setCategoryId(product.getCategory().getId());
            productDto.setCategoryName(product.getCategory().getName());
        }

        return productDto;
    }
}