package com.blooming.blockchain.springbackend.wallet.repository;

import com.blooming.blockchain.springbackend.wallet.entity.UserWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserWalletRepository extends JpaRepository<UserWallet, UUID> {
    
    /**
     * Find the active wallet for a user
     * @param userId the user ID
     * @return Optional containing the active wallet, or empty if none found
     */
    @Query("SELECT uw FROM UserWallet uw WHERE uw.userId = :userId AND uw.isActive = true ORDER BY uw.createdAt DESC")
    Optional<UserWallet> findActiveWalletByUserId(@Param("userId") UUID userId);

    /**
     * Find wallet by wallet address
     * @param walletAddress the wallet address to search for
     * @return Optional containing the wallet, or empty if none found
     */
    Optional<UserWallet> findByWalletAddress(String walletAddress);

    /**
     * Find active wallet by wallet address
     * @param walletAddress the wallet address to search for
     * @return Optional containing the active wallet, or empty if none found
     */
    @Query("SELECT uw FROM UserWallet uw WHERE uw.walletAddress = :walletAddress AND uw.isActive = true")
    Optional<UserWallet> findActiveWalletByAddress(@Param("walletAddress") String walletAddress);

    /**
     * Check if user has any active wallet
     * @param userId the user ID
     * @return true if user has at least one active wallet
     */
    @Query("SELECT COUNT(uw) > 0 FROM UserWallet uw WHERE uw.userId = :userId AND uw.isActive = true")
    boolean hasActiveWallet(@Param("userId") UUID userId);

    /**
     * Check if user has any wallet (active or inactive)
     * @param userId the user ID
     * @return true if user has at least one wallet
     */
    boolean existsByUserId(UUID userId);

    /**
     * Deactivate all wallets for a user (for wallet replacement scenarios)
     * @param userId the user ID
     * @return number of wallets deactivated
     */
    @Query("UPDATE UserWallet uw SET uw.isActive = false WHERE uw.userId = :userId AND uw.isActive = true")
    int deactivateAllUserWallets(@Param("userId") UUID userId);
}