package com.blooming.blockchain.springbackend.pointtransaction.repository;

import com.blooming.blockchain.springbackend.pointtransaction.entity.TokenTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenTransactionRepository extends JpaRepository<TokenTransaction, Integer> {

    // Find by transaction hash
    Optional<TokenTransaction> findByTxHash(String txHash);
    
    // Find by user (ordered by most recent first)
    List<TokenTransaction> findByUserGoogleIdOrderByCreatedAtDesc(String userGoogleId);
    
    // Find pending transactions for user
    @Query("SELECT t FROM TokenTransaction t WHERE t.userGoogleId = :userGoogleId AND t.transactionStatusId = 1")
    List<TokenTransaction> findPendingTransactionsByUser(@Param("userGoogleId") String userGoogleId);
    
    // Find confirmed transactions for user
    @Query("SELECT t FROM TokenTransaction t WHERE t.userGoogleId = :userGoogleId AND t.transactionStatusId = 2")
    List<TokenTransaction> findConfirmedTransactionsByUser(@Param("userGoogleId") String userGoogleId);
    
    // Get user statistics
    @Query("SELECT COALESCE(SUM(t.mainPointsSpent), 0) FROM TokenTransaction t WHERE t.userGoogleId = :userGoogleId AND t.transactionStatusId = 2")
    Integer getTotalMainPointsExchangedByUser(@Param("userGoogleId") String userGoogleId);
    
    @Query("SELECT COALESCE(SUM(t.tokensReceived), 0) FROM TokenTransaction t WHERE t.userGoogleId = :userGoogleId AND t.transactionStatusId = 2")
    Long getTotalTokensReceivedByUser(@Param("userGoogleId") String userGoogleId);
}