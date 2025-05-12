package com.ecommerce.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "admins")
public class Admin extends User {

    @Column(name = "approval_seller_status")
    private Boolean approveSellerStatus;

    @Column(name = "ban_user_status")
    private Boolean banUserStatus;
}
