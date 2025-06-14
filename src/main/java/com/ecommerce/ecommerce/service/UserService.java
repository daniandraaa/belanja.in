package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.entity.User;
import com.ecommerce.ecommerce.model.UserDto;
import com.ecommerce.ecommerce.model.UserUpdateDto;
import com.ecommerce.ecommerce.model.ChangePasswordDto;
import com.ecommerce.ecommerce.repository.StoreRepository;
import com.ecommerce.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserDto getCurrentUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return mapToDto(user);
    }

    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return mapToDto(user);
    }    public UserDto updateProfile(UserUpdateDto updateDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if ((updateDto.getName() == null || updateDto.getName().trim().isEmpty()) &&
                (updateDto.getEmail() == null || updateDto.getEmail().trim().isEmpty())) {
            throw new RuntimeException("At least one field (name or email) must be provided");
        }


        if (updateDto.getName() != null && !updateDto.getName().trim().isEmpty()) {
            user.setName(updateDto.getName().trim());
        }


        if (updateDto.getEmail() != null && !updateDto.getEmail().trim().isEmpty()) {
            if (userRepository.findByEmail(updateDto.getEmail()).isPresent() &&
                    !user.getEmail().equals(updateDto.getEmail())) {
                throw new RuntimeException("Email is already taken by another user");
            }
            user.setEmail(updateDto.getEmail().trim());
        }

        User updatedUser = userRepository.save(user);
        return mapToDto(updatedUser);
    }

    public void changePassword(ChangePasswordDto changePasswordDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));


        if (!passwordEncoder.matches(changePasswordDto.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }


        if (!changePasswordDto.getNewPassword().equals(changePasswordDto.getConfirmPassword())) {
            throw new RuntimeException("New password and confirm password do not match");
        }


        user.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        userRepository.save(user);
    }

    private UserDto mapToDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setRoles(user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet()));
        userDto.setHasStore(storeRepository.findByOwner(user).isPresent());

        if (user.getStore() != null) {
            userDto.setStoreUsername(user.getStore().getUsername());
        }

        return userDto;
    }
}