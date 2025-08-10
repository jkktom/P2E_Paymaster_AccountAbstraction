package com.blooming.blockchain.springbackend.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users", indexes = {
        @Index(name = "idx_users_google_id", columnList = "googleId"),
        @Index(name = "idx_users_wallet_address", columnList = "smartWalletAddress"),
        @Index(name = "idx_users_role", columnList = "roleId")
})
public class User {
    @Id
    private UUID id = UUID.randomUUID();

    @Column(unique = true, nullable = false)
    private String googleId;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String avatar;

    @Column(name = "smart_wallet_address", unique = true, nullable = false, length = 42)
    private String smartWalletAddress;

    @Column(name = "role_id", nullable = false)
    private Byte roleId = 2; // Default to USER role

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
