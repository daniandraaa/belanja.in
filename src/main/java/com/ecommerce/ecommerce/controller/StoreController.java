package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.model.StoreDto;
import com.ecommerce.ecommerce.service.StoreService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
@CrossOrigin(origins = "*")
public class StoreController {

    @Autowired
    private StoreService storeService;

    @PostMapping
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<StoreDto> createStore(@Valid @RequestBody StoreDto storeDto) {
        try {
            StoreDto createdStore = storeService.createStore(storeDto);
            return new ResponseEntity<>(createdStore, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{username}")
    public ResponseEntity<StoreDto> getStoreByUsername(@PathVariable String username) {
        try {
            StoreDto storeDto = storeService.getStoreByUsername(username);
            return ResponseEntity.ok(storeDto);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<StoreDto>> getAllStores() {
        List<StoreDto> stores = storeService.getAllStores();
        return ResponseEntity.ok(stores);
    }

    @GetMapping("/search")
    public ResponseEntity<List<StoreDto>> searchStores(@RequestParam String name) {
        List<StoreDto> stores = storeService.searchStores(name);
        return ResponseEntity.ok(stores);
    }
}