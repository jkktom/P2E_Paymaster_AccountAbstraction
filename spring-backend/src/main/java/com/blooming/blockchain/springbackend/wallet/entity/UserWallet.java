package com.blooming.blockchain.springbackend.wallet.entity;

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
@Table(name = "user_wallets", indexes = {
        @Index(name = "idx_user_wallets_user_id", columnList = "userId"),
        @Index(name = "idx_user_wallets_wallet_address", columnList = "walletAddress")
})
public class UserWallet {
    
    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "wallet_address", nullable = false, length = 42, unique = true)
    private String walletAddress;

    @Column(name = "encrypted_private_key", nullable = false, length = 500)
    private String encryptedPrivateKey;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Business methods
    public String getShortAddress() {
        if (walletAddress == null || walletAddress.length() < 10) {
            return walletAddress;
        }
        return walletAddress.substring(0, 6) + "..." + walletAddress.substring(walletAddress.length() - 4);
    }

    public boolean isReadyForTransactions() {
        return isActive && walletAddress != null && 
               encryptedPrivateKey != null && !encryptedPrivateKey.trim().isEmpty();
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }
}