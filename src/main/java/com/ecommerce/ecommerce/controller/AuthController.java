package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.model.JwtAuthResponse;
import com.ecommerce.ecommerce.model.LoginDto;
import com.ecommerce.ecommerce.model.RegisterDto;
import com.ecommerce.ecommerce.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@Valid @RequestBody LoginDto loginDto) {
        try {
            JwtAuthResponse response = authService.login(loginDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Invalid username/email or password");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterDto registerDto) {
        try {
            String message = authService.register(registerDto);
            return new ResponseEntity<>(message, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}