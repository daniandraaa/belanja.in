package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.model.UserDto;
import com.ecommerce.ecommerce.model.UserUpdateDto;
import com.ecommerce.ecommerce.model.ChangePasswordDto;
import com.ecommerce.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('BUYER') or hasRole('SELLER')")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        try {
            UserDto userDto = userService.getCurrentUserInfo();
            return ResponseEntity.ok(userDto);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId) {
        try {
            UserDto userDto = userService.getUserById(userId);
            return ResponseEntity.ok(userDto);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UserUpdateDto updateDto) {
        try {
            UserDto updatedUser = userService.updateProfile(updateDto);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now()
            ));
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordDto changePasswordDto) {
        try {
            userService.changePassword(changePasswordDto);
            return ResponseEntity.ok("Password changed successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}