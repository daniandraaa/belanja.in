package com.ecommerce.ecommerce.config;

import com.ecommerce.ecommerce.entity.Role;
import com.ecommerce.ecommerce.entity.RoleName;
import com.ecommerce.ecommerce.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        // Initialize roles if they don't exist
        if (roleRepository.count() == 0) {
            Role buyerRole = new Role();
            buyerRole.setName(RoleName.BUYER);
            roleRepository.save(buyerRole);

            Role sellerRole = new Role();
            sellerRole.setName(RoleName.SELLER);
            roleRepository.save(sellerRole);

            System.out.println("Roles initialized: BUYER and SELLER");
        }
    }
}