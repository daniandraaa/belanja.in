package com.ecommerce.ecommerce.config;

import com.ecommerce.ecommerce.security.CustomUserDetailsService;
import com.ecommerce.ecommerce.security.JwtAuthenticationEntryPoint;
import com.ecommerce.ecommerce.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/stores/**").permitAll()

                        // User endpoints
                        .requestMatchers("/api/users/me").hasAnyRole("BUYER", "SELLER")

                        // Store creation (only BUYER can create store to become SELLER)
                        .requestMatchers(HttpMethod.POST, "/api/stores").hasRole("BUYER")

                        // Product management (only SELLER can manage products)
                        .requestMatchers(HttpMethod.POST, "/api/products").hasRole("SELLER")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("SELLER")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("SELLER")

                        // Category management (any authenticated user can create categories)
                        .requestMatchers(HttpMethod.POST, "/api/categories").hasAnyRole("BUYER", "SELLER")

                        // Order management
                        .requestMatchers(HttpMethod.POST, "/api/orders").hasAnyRole("BUYER", "SELLER")
                        .requestMatchers(HttpMethod.GET, "/api/orders/my-orders").hasAnyRole("BUYER", "SELLER")

                        // All other requests require authentication
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}