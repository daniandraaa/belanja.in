package com.ecommerce.ecommerce.model;

import com.ecommerce.ecommerce.entity.RoleName;
import lombok.Data;

import java.util.Set;

@Data
public class UserDto {
    private Long id;
    private String name;
    private String username;
    private String email;
    private Set<RoleName> roles;
    private Boolean hasStore;
    private String storeUsername;
}