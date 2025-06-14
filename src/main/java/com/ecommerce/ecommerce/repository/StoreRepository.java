package com.ecommerce.ecommerce.repository;

import com.ecommerce.ecommerce.entity.Store;
import com.ecommerce.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    Optional<Store> findByUsername(String username);
    Optional<Store> findByOwner(User owner);
    Boolean existsByUsername(String username);


    List<Store> findByNameContainingIgnoreCase(String name);


    List<Store> findByDescriptionContainingIgnoreCase(String description);
    List<Store> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);
}