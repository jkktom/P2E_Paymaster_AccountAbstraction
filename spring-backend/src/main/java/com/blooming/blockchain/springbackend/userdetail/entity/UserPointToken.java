package com.blooming.blockchain.springbackend.userdetail.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_point_tokens", indexes = {
    @Index(name = "idx_user_point_token_google_id", columnList = "userGoogleId")
})
public class UserPointToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_google_id", nullable = false, unique = true, length = 50)
    private String userGoogleId;

    @Column(name = "main_point", nullable = false)
    @Min(value = 0, message = "Main points cannot be negative")
    private Integer mainPoint = 0;

    @Column(name = "sub_point", nullable = false)
    @Min(value = 0, message = "Sub points cannot be negative")
    private Integer subPoint = 0;

    @Column(name = "token_balance", nullable = false)
    @Min(value = 0, message = "Token balance cannot be negative")
    private Long tokenBalance = 0L;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Business constructor for creating new user balance
    public UserPointToken(String userGoogleId, Integer mainPoint, Integer subPoint, Long tokenBalance) {
        this.userGoogleId = userGoogleId;
        this.mainPoint = mainPoint;
        this.subPoint = subPoint;
        this.tokenBalance = tokenBalance;
    }

    // Convenience constructor with zero balances
    public UserPointToken(String userGoogleId) {
        this(userGoogleId, 0, 0, 0L);
    }

    // Business methods for updating individual balances
    public void addMainPoints(Integer amount) {
        if (amount > 0) {
            this.mainPoint += amount;
        }
    }

    public void subtractMainPoints(Integer amount) {
        if (amount > 0 && this.mainPoint >= amount) {
            this.mainPoint -= amount;
        } else {
            throw new IllegalArgumentException("Insufficient main points or invalid amount");
        }
    }

    public void addSubPoints(Integer amount) {
        if (amount > 0) {
            this.subPoint += amount;
        }
    }

    public void subtractSubPoints(Integer amount) {
        if (amount > 0 && this.subPoint >= amount) {
            this.subPoint -= amount;
        } else {
            throw new IllegalArgumentException("Insufficient sub points or invalid amount");
        }
    }

    public void addTokens(Long amount) {
        if (amount > 0) {
            this.tokenBalance += amount;
        }
    }

    public void subtractTokens(Long amount) {
        if (amount > 0 && this.tokenBalance >= amount) {
            this.tokenBalance -= amount;
        } else {
            throw new IllegalArgumentException("Insufficient token balance or invalid amount");
        }
    }

    // Atomic conversion operations
    public void convertSubToMainPoints(Integer subAmount, Integer mainAmount) {
        if (subAmount > 0 && mainAmount > 0 && this.subPoint >= subAmount) {
            this.subPoint -= subAmount;
            this.mainPoint += mainAmount;
        } else {
            throw new IllegalArgumentException("Invalid conversion amounts or insufficient sub points");
        }
    }

    public void exchangeMainPointsToTokens(Integer mainAmount, Long tokenAmount) {
        if (mainAmount > 0 && tokenAmount > 0 && this.mainPoint >= mainAmount) {
            this.mainPoint -= mainAmount;
            this.tokenBalance += tokenAmount;
        } else {
            throw new IllegalArgumentException("Invalid exchange amounts or insufficient main points");
        }
    }

    // Validation methods
    public boolean hasSufficientMainPoints(Integer requiredAmount) {
        return this.mainPoint >= requiredAmount;
    }

    public boolean hasSufficientSubPoints(Integer requiredAmount) {
        return this.subPoint >= requiredAmount;
    }

    public boolean hasSufficientTokens(Long requiredAmount) {
        return this.tokenBalance >= requiredAmount;
    }

    // Calculate total point value (for statistics)
    public Integer getTotalPointValue() {
        return this.mainPoint + this.subPoint;
    }

    // Check if user has any balance
    public boolean hasAnyBalance() {
        return this.mainPoint > 0 || this.subPoint > 0 || this.tokenBalance > 0;
    }

    // Override toString for debugging
    @Override
    public String toString() {
        return String.format("UserPointToken{id=%d, userGoogleId='%s', mainPoint=%d, subPoint=%d, tokenBalance=%d, createdAt=%s, updatedAt=%s}",
                id, userGoogleId, mainPoint, subPoint, tokenBalance, createdAt, updatedAt);
    }

    // Override equals and hashCode based on userGoogleId (business key)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserPointToken that)) return false;
        return userGoogleId != null && userGoogleId.equals(that.userGoogleId);
    }

    @Override
    public int hashCode() {
        return userGoogleId != null ? userGoogleId.hashCode() : 0;
    }
}
