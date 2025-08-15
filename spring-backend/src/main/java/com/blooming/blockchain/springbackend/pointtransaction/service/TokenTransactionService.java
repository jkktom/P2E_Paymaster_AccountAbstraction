package com.blooming.blockchain.springbackend.pointtransaction.service;

import com.blooming.blockchain.springbackend.pointtransaction.entity.TokenTransaction;
import com.blooming.blockchain.springbackend.pointtransaction.repository.TokenTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TokenTransactionService {

    private final TokenTransactionRepository tokenTransactionRepository;

    /**
     * Create a new token transaction record
     */
    public TokenTransaction createTokenTransaction(String userGoogleId, Integer mainPointsSpent, 
                                                 Long tokensReceived, String description) {
        try {
            TokenTransaction transaction = new TokenTransaction(userGoogleId, mainPointsSpent, tokensReceived, description);
            TokenTransaction saved = tokenTransactionRepository.save(transaction);
            
            log.info("Created token transaction record - User: {}, ID: {}, Main Points: {}, Tokens: {}", 
                userGoogleId, saved.getId(), mainPointsSpent, tokensReceived);
            
            return saved;
        } catch (Exception e) {
            log.error("Failed to create token transaction record for user: {}", userGoogleId, e);
            throw new RuntimeException("Failed to create token transaction record", e);
        }
    }

    /**
     * Confirm a token transaction with blockchain hash
     */
    public boolean confirmTokenTransaction(Integer transactionId, String txHash) {
        try {
            Optional<TokenTransaction> transactionOpt = tokenTransactionRepository.findById(transactionId);
            if (transactionOpt.isEmpty()) {
                log.error("Token transaction not found with ID: {}", transactionId);
                return false;
            }

            TokenTransaction transaction = transactionOpt.get();
            transaction.confirmTransaction(txHash);
            tokenTransactionRepository.save(transaction);
            
            log.info("Confirmed token transaction - ID: {}, TX Hash: {}", transactionId, txHash);
            return true;
        } catch (Exception e) {
            log.error("Failed to confirm token transaction ID: {}", transactionId, e);
            return false;
        }
    }

    /**
     * Fail a token transaction
     */
    public boolean failTokenTransaction(Integer transactionId) {
        try {
            Optional<TokenTransaction> transactionOpt = tokenTransactionRepository.findById(transactionId);
            if (transactionOpt.isEmpty()) {
                log.error("Token transaction not found with ID: {}", transactionId);
                return false;
            }

            TokenTransaction transaction = transactionOpt.get();
            transaction.failTransaction();
            tokenTransactionRepository.save(transaction);
            
            log.info("Failed token transaction - ID: {}", transactionId);
            return true;
        } catch (Exception e) {
            log.error("Failed to mark token transaction as failed ID: {}", transactionId, e);
            return false;
        }
    }

    /**
     * Get user's token transaction history
     */
    @Transactional(readOnly = true)
    public List<TokenTransaction> getUserTokenTransactions(String userGoogleId) {
        return tokenTransactionRepository.findByUserGoogleIdOrderByCreatedAtDesc(userGoogleId);
    }

    /**
     * Get user token exchange statistics
     */
    @Transactional(readOnly = true)
    public UserTokenStatistics getUserTokenStatistics(String userGoogleId) {
        Integer totalMainPointsExchanged = tokenTransactionRepository.getTotalMainPointsExchangedByUser(userGoogleId);
        Long totalTokensReceived = tokenTransactionRepository.getTotalTokensReceivedByUser(userGoogleId);
        List<TokenTransaction> pendingTransactions = tokenTransactionRepository.findPendingTransactionsByUser(userGoogleId);
        List<TokenTransaction> confirmedTransactions = tokenTransactionRepository.findConfirmedTransactionsByUser(userGoogleId);

        return new UserTokenStatistics(
            totalMainPointsExchanged != null ? totalMainPointsExchanged : 0,
            totalTokensReceived != null ? totalTokensReceived : 0L,
            pendingTransactions.size(),
            confirmedTransactions.size()
        );
    }

    // Inner class for user statistics
    public static class UserTokenStatistics {
        private final Integer totalMainPointsExchanged;
        private final Long totalTokensReceived;
        private final Integer pendingTransactionCount;
        private final Integer confirmedTransactionCount;

        public UserTokenStatistics(Integer totalMainPointsExchanged, Long totalTokensReceived, 
                                 Integer pendingTransactionCount, Integer confirmedTransactionCount) {
            this.totalMainPointsExchanged = totalMainPointsExchanged;
            this.totalTokensReceived = totalTokensReceived;
            this.pendingTransactionCount = pendingTransactionCount;
            this.confirmedTransactionCount = confirmedTransactionCount;
        }

        // Getters
        public Integer getTotalMainPointsExchanged() { return totalMainPointsExchanged; }
        public Long getTotalTokensReceived() { return totalTokensReceived; }
        public Integer getPendingTransactionCount() { return pendingTransactionCount; }
        public Integer getConfirmedTransactionCount() { return confirmedTransactionCount; }
    }
}