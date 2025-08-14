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
        @Index(name = "idx_users_google_id", columnList = "googleId")
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

    @Column(name = "avatar", length = 500)
    private String avatar;

    @Column(name = "smart_wallet_address", length = 42, unique = true)
    private String smartWalletAddress;

    @Column(name = "role_id", nullable = false)
    private Byte roleId;

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

    public boolean hasSmartWallet() {
        return this.smartWalletAddress != null && !this.smartWalletAddress.trim().isEmpty();
    }
}
