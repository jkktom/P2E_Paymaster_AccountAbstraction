package com.blooming.blockchain.springbackend.userdetail.repository;

import com.blooming.blockchain.springbackend.userdetail.entity.UserPointToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPointTokenRepository extends JpaRepository<UserPointToken, Integer> {

    // Find user balance by Google ID
    Optional<UserPointToken> findByUserGoogleId(String userGoogleId);

    // Check if user balance record exists
    boolean existsByUserGoogleId(String userGoogleId);

    // Add main points to user balance
    @Modifying
    @Query("UPDATE UserPointToken u SET u.mainPoint = u.mainPoint + :amount WHERE u.userGoogleId = :userGoogleId")
    int addMainPoints(@Param("userGoogleId") String userGoogleId, @Param("amount") Integer amount);

    // Add sub points to user balance
    @Modifying
    @Query("UPDATE UserPointToken u SET u.subPoint = u.subPoint + :amount WHERE u.userGoogleId = :userGoogleId")
    int addSubPoints(@Param("userGoogleId") String userGoogleId, @Param("amount") Integer amount);

    // Spend main points (for token exchange)
    @Modifying
    @Query("UPDATE UserPointToken u SET u.mainPoint = u.mainPoint - :amount WHERE u.userGoogleId = :userGoogleId AND u.mainPoint >= :amount")
    int spendMainPoints(@Param("userGoogleId") String userGoogleId, @Param("amount") Integer amount);

    // Spend sub points (for main point conversion)
    @Modifying
    @Query("UPDATE UserPointToken u SET u.subPoint = u.subPoint - :amount WHERE u.userGoogleId = :userGoogleId AND u.subPoint >= :amount")
    int spendSubPoints(@Param("userGoogleId") String userGoogleId, @Param("amount") Integer amount);

    // Add tokens to user balance
    @Modifying
    @Query("UPDATE UserPointToken u SET u.tokenBalance = u.tokenBalance + :amount WHERE u.userGoogleId = :userGoogleId")
    int addTokens(@Param("userGoogleId") String userGoogleId, @Param("amount") Long amount);

    // Convert sub to main points atomically
    @Modifying
    @Query("UPDATE UserPointToken u SET u.subPoint = u.subPoint - :subAmount, u.mainPoint = u.mainPoint + :mainAmount WHERE u.userGoogleId = :userGoogleId AND u.subPoint >= :subAmount")
    int convertSubToMainPoints(@Param("userGoogleId") String userGoogleId, @Param("subAmount") Integer subAmount, @Param("mainAmount") Integer mainAmount);

    // Exchange main points to tokens atomically
    @Modifying
    @Query("UPDATE UserPointToken u SET u.mainPoint = u.mainPoint - :mainAmount, u.tokenBalance = u.tokenBalance + :tokenAmount WHERE u.userGoogleId = :userGoogleId AND u.mainPoint >= :mainAmount")
    int exchangeMainPointsToTokens(@Param("userGoogleId") String userGoogleId, @Param("mainAmount") Integer mainAmount, @Param("tokenAmount") Long tokenAmount);

    // Get user's main point balance
    @Query("SELECT u.mainPoint FROM UserPointToken u WHERE u.userGoogleId = :userGoogleId")
    Optional<Integer> getMainPointBalance(@Param("userGoogleId") String userGoogleId);

    // Get user's sub point balance
    @Query("SELECT u.subPoint FROM UserPointToken u WHERE u.userGoogleId = :userGoogleId")
    Optional<Integer> getSubPointBalance(@Param("userGoogleId") String userGoogleId);

    // Get user's token balance
    @Query("SELECT u.tokenBalance FROM UserPointToken u WHERE u.userGoogleId = :userGoogleId")
    Optional<Long> getTokenBalance(@Param("userGoogleId") String userGoogleId);
}