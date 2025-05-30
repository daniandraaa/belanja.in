package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.entity.Role;
import com.ecommerce.ecommerce.entity.RoleName;
import com.ecommerce.ecommerce.entity.User;
import com.ecommerce.ecommerce.model.JwtAuthResponse;
import com.ecommerce.ecommerce.model.LoginDto;
import com.ecommerce.ecommerce.model.RegisterDto;
import com.ecommerce.ecommerce.repository.RoleRepository;
import com.ecommerce.ecommerce.repository.UserRepository;
import com.ecommerce.ecommerce.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    public JwtAuthResponse login(LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getUsernameOrEmail(),
                        loginDto.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        return new JwtAuthResponse(token);
    }

    public String register(RegisterDto registerDto) {
        if (userRepository.existsByUsername(registerDto.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }

        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new RuntimeException("Email is already registered!");
        }

        User user = new User();
        user.setName(registerDto.getName());
        user.setUsername(registerDto.getUsername());
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        Role buyerRole = roleRepository.findByName(RoleName.BUYER)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.setRoles(new HashSet<>(Collections.singletonList(buyerRole)));
        userRepository.save(user);

        return "User registered successfully";
    }
}