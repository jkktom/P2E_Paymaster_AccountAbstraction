package com.blooming.blockchain.springbackend.user.entity;

import com.blooming.blockchain.springbackend.global.enums.RoleType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
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
        return this.roleId.equals(RoleType.ADMIN.getId());
    }

    public boolean isUser() {
        return this.roleId.equals(RoleType.USER.getId());
    }

    public RoleType getRoleType() {
        return RoleType.fromId(this.roleId);
    }

    public boolean hasSmartWallet() {
        return this.smartWalletAddress != null && !this.smartWalletAddress.trim().isEmpty();
    }

    // Controlled update methods for legitimate business operations
    public void updateEmail(String email) {
        if (email != null && !email.trim().isEmpty()) {
            this.email = email;
        }
    }

    public void updateName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
        }
    }

    public void updateAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void updateSmartWalletAddress(String smartWalletAddress) {
        if (smartWalletAddress != null && !smartWalletAddress.trim().isEmpty()) {
            this.smartWalletAddress = smartWalletAddress;
        }
    }

    public void setRole(RoleType roleType) {
        if (roleType != null) {
            this.roleId = roleType.getId();
        }
    }
}
