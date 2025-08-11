package com.blooming.blockchain.springbackend.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "users", indexes = {
        @Index(name = "idx_users_google_id", columnList = "googleId"),
        @Index(name = "idx_users_wallet_address", columnList = "smartWalletAddress"),
        @Index(name = "idx_users_role", columnList = "roleId")
})
public class User {
    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column(unique = true, nullable = false, length = 50)
    private String googleId;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String avatar;

    @Column(name = "smart_wallet_address", unique = true, nullable = false, length = 42)
    private String smartWalletAddress;

    @Column(name = "role_id", nullable = false)
    @Builder.Default
    private Byte roleId = 2; // Default to USER role (2 = USER, 1 = ADMIN)

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Business methods
    public boolean isAdmin() {
        return this.roleId == 1;
    }

    public boolean isUser() {
        return this.roleId == 2;
    }
}
