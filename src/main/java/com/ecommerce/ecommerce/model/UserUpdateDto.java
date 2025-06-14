package com.ecommerce.ecommerce.model;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Data
public class UserUpdateDto {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Email(message = "Please provide a valid email")
    private String email;
}
