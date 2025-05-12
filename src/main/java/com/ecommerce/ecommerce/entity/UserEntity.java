package com.ecommerce.ecommerce.entity;
import  jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity {
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
