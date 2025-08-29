package com.blooming.blockchain.springbackend.pointtransaction.entity;

import com.blooming.blockchain.springbackend.global.enums.TransactionStatusType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "token_transactions", indexes = {
    @Index(name = "idx_token_tx_user_date", columnList = "userGoogleId, createdAt"),
    @Index(name = "idx_token_tx_hash", columnList = "txHash"),
    @Index(name = "idx_token_tx_status", columnList = "transactionStatusId")
})
@Getter
@Setter
@NoArgsConstructor
public class TokenTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_google_id", nullable = false)
    private String userGoogleId;

    @Column(name = "main_points_spent", nullable = false)
    @Min(10) // Minimum 10 main points for 1 token
    private Integer mainPointsSpent;

    @Column(name = "tokens_received", nullable = false)
    @Min(1)
    private Long tokensReceived; // Using long to match UserPointToken.tokenBalance

    @Column(name = "tx_hash", unique = true, length = 66)
    private String txHash; // zkSync transaction hash

    @Column(name = "transaction_status_id", nullable = false)
    private Byte transactionStatusId = TransactionStatusType.PENDING.getId(); // Default to PENDING

    @Column(name = "gas_sponsored", nullable = false)
    private Boolean gasSponsored = true; // Always true for zkSync AA

    @Column(name = "applied_to_balance", nullable = false)
    private Boolean appliedToBalance = false; // Applied to UserPointToken entity

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime confirmedAt;

    // Constructor for creating token transactions
    public TokenTransaction(String userGoogleId, Integer mainPointsSpent, Long tokensReceived, String description) {
        this.userGoogleId = userGoogleId;
        this.mainPointsSpent = mainPointsSpent;
        this.tokensReceived = tokensReceived;
        this.description = description;
        this.transactionStatusId = TransactionStatusType.PENDING.getId();
        this.gasSponsored = true;
        this.appliedToBalance = false;
    }

    // Business rule validation
    @PrePersist
    @PreUpdate
    private void validateExchangeRate() {
        // Validate exchange rate: 10 main points = 1 token
        if (mainPointsSpent != tokensReceived * 10) {
            throw new IllegalStateException(
                String.format("Exchange rate must be 10:1 (main points:tokens). Got %d points for %d tokens", 
                    mainPointsSpent, tokensReceived)
            );
        }
    }

    // Helper method to confirm transaction with blockchain hash
    public void confirmTransaction(String txHash) {
        this.txHash = txHash;
        this.transactionStatusId = TransactionStatusType.CONFIRMED.getId();
        this.appliedToBalance = true;
        this.confirmedAt = LocalDateTime.now();
    }

    // Helper method to fail transaction
    public void failTransaction() {
        this.transactionStatusId = TransactionStatusType.FAILED.getId();
    }

    // Check if transaction is confirmed
    public boolean isConfirmed() {
        return this.transactionStatusId == TransactionStatusType.CONFIRMED.getId();
    }

    // Check if transaction is pending
    public boolean isPending() {
        return this.transactionStatusId == TransactionStatusType.PENDING.getId();
    }
}
