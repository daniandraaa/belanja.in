package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.entity.Role;
import com.ecommerce.ecommerce.entity.RoleName;
import com.ecommerce.ecommerce.entity.Store;
import com.ecommerce.ecommerce.entity.User;
import com.ecommerce.ecommerce.model.StoreDto;
import com.ecommerce.ecommerce.repository.RoleRepository;
import com.ecommerce.ecommerce.repository.StoreRepository;
import com.ecommerce.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    public StoreDto createStore(StoreDto storeDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (storeRepository.findByOwner(user).isPresent()) {
            throw new RuntimeException("User already has a store");
        }

        if (storeRepository.existsByUsername(storeDto.getUsername())) {
            throw new RuntimeException("Store username is already taken");
        }

        Store store = new Store();
        store.setUsername(storeDto.getUsername());
        store.setName(storeDto.getName());
        store.setAddress(storeDto.getAddress());
        store.setDescription(storeDto.getDescription());
        store.setOwner(user);

        Store savedStore = storeRepository.save(store);

        // Add SELLER role to user
        Role sellerRole = roleRepository.findByName(RoleName.SELLER)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.getRoles().add(sellerRole);
        userRepository.save(user);

        return mapToDto(savedStore);
    }

    public StoreDto getStoreByUsername(String username) {
        Store store = storeRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        return mapToDto(store);
    }

    public List<StoreDto> getAllStores() {
        return storeRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<StoreDto> searchStores(String name) {
        return storeRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private StoreDto mapToDto(Store store) {
        StoreDto storeDto = new StoreDto();
        storeDto.setId(store.getId());
        storeDto.setUsername(store.getUsername());
        storeDto.setName(store.getName());
        storeDto.setAddress(store.getAddress());
        storeDto.setDescription(store.getDescription());
        storeDto.setOwnerUsername(store.getOwner().getUsername());
        storeDto.setOwnerName(store.getOwner().getName());
        storeDto.setProductCount(store.getProducts().size());
        return storeDto;
    }
}