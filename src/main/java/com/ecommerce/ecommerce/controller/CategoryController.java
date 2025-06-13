//package com.ecommerce.ecommerce.controller;
//
//import com.ecommerce.ecommerce.model.CategoryDto;
//import com.ecommerce.ecommerce.service.CategoryService;
//import jakarta.validation.Valid;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/categories")
//@CrossOrigin(origins = "*")
//public class CategoryController {
//
//    @Autowired
//    private CategoryService categoryService;
//
//    @PostMapping
//    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
//    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryDto categoryDto) {
//        try {
//            CategoryDto createdCategory = categoryService.createCategory(categoryDto);
//            return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
//        } catch (RuntimeException e) {
//            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
//        }
//    }
//
//    @GetMapping
//    public ResponseEntity<List<CategoryDto>> getAllCategories() {
//        List<CategoryDto> categories = categoryService.getAllCategories();
//        return ResponseEntity.ok(categories);
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long id) {
//        try {
//            CategoryDto categoryDto = categoryService.getCategoryById(id);
//            return ResponseEntity.ok(categoryDto);
//        } catch (RuntimeException e) {
//            return ResponseEntity.notFound().build();
//        }
//    }
//
//    @PutMapping("/{id}")
//    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
//    public ResponseEntity<CategoryDto> updateCategory(@PathVariable Long id,
//                                                      @Valid @RequestBody CategoryDto categoryDto) {
//        try {
//            CategoryDto updatedCategory = categoryService.updateCategory(id, categoryDto);
//            return ResponseEntity.ok(updatedCategory);
//        } catch (RuntimeException e) {
//            return ResponseEntity.badRequest().build();
//        }
//    }
//
//    @DeleteMapping("/{id}")
//    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
//    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
//        try {
//            categoryService.deleteCategory(id);
//            return ResponseEntity.noContent().build();
//        } catch (RuntimeException e) {
//            return ResponseEntity.badRequest().build();
//        }
//    }
//}