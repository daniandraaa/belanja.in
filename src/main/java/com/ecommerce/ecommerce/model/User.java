package com.ecommerce.ecommerce.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Entity
@Table(name = "users")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "status")
    private String status;

    public boolean login(String username, String password) {
        // Implementasi login logic
        return this.username.equals(username) && this.password.equals(password);
    }

    public void logout() {
        // Implementasi logout
    }
}
