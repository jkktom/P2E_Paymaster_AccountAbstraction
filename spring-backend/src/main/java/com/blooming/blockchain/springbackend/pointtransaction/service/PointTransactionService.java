package com.blooming.blockchain.springbackend.pointtransaction.service;

import com.blooming.blockchain.springbackend.global.enums.TransactionStatusType;
import com.blooming.blockchain.springbackend.pointtransaction.entity.PointTransaction;
import com.blooming.blockchain.springbackend.pointtransaction.repository.PointTransactionRepository;
import com.blooming.blockchain.springbackend.userdetail.service.UserPointTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PointTransactionService {

    private final PointTransactionRepository pointTransactionRepository;
    private final UserPointTokenService userPointTokenService;

    // Earn main points
    public PointTransaction earnMainPoints(String userGoogleId, Integer amount, Byte sourceId, String description) {
        // Create point transaction
        PointTransaction transaction = description != null 
            ? PointTransaction.createMainEarn(userGoogleId, amount, sourceId, description)
            : PointTransaction.createMainEarn(userGoogleId, amount, sourceId);
        
        // Save transaction as pending
        PointTransaction savedTransaction = pointTransactionRepository.save(transaction);
        
        try {
            // Apply to user balance
            boolean success = userPointTokenService.addMainPoints(userGoogleId, amount);
            
            if (success) {
                // Confirm transaction
                savedTransaction.confirmTransaction();
                pointTransactionRepository.save(savedTransaction);
                log.info("Main points earned successfully. User: {}, Amount: {}, Source: {}", userGoogleId, amount, sourceId);
            } else {
                // Fail transaction
                savedTransaction.failTransaction();
                pointTransactionRepository.save(savedTransaction);
                log.error("Failed to apply main points to balance. User: {}, Amount: {}", userGoogleId, amount);
            }
        } catch (Exception e) {
            // Fail transaction on exception
            savedTransaction.failTransaction();
            pointTransactionRepository.save(savedTransaction);
            log.error("Exception during main point earning. User: {}, Amount: {}", userGoogleId, amount, e);
        }
        
        return savedTransaction;
    }

    // Earn main points (without description)
    public PointTransaction earnMainPoints(String userGoogleId, Integer amount, Byte sourceId) {
        return earnMainPoints(userGoogleId, amount, sourceId, null);
    }

    // Earn sub points
    public PointTransaction earnSubPoints(String userGoogleId, Integer amount, Byte sourceId, String description) {
        // Create point transaction
        PointTransaction transaction = description != null 
            ? PointTransaction.createSubEarn(userGoogleId, amount, sourceId, description)
            : PointTransaction.createSubEarn(userGoogleId, amount, sourceId);
        
        // Save transaction as pending
        PointTransaction savedTransaction = pointTransactionRepository.save(transaction);
        
        try {
            // Apply to user balance
            boolean success = userPointTokenService.addSubPoints(userGoogleId, amount);
            
            if (success) {
                // Confirm transaction
                savedTransaction.confirmTransaction();
                pointTransactionRepository.save(savedTransaction);
                log.info("Sub points earned successfully. User: {}, Amount: {}, Source: {}", userGoogleId, amount, sourceId);
            } else {
                // Fail transaction
                savedTransaction.failTransaction();
                pointTransactionRepository.save(savedTransaction);
                log.error("Failed to apply sub points to balance. User: {}, Amount: {}", userGoogleId, amount);
            }
        } catch (Exception e) {
            // Fail transaction on exception
            savedTransaction.failTransaction();
            pointTransactionRepository.save(savedTransaction);
            log.error("Exception during sub point earning. User: {}, Amount: {}", userGoogleId, amount, e);
        }
        
        return savedTransaction;
    }

    // Earn sub points (without description)
    public PointTransaction earnSubPoints(String userGoogleId, Integer amount, Byte sourceId) {
        return earnSubPoints(userGoogleId, amount, sourceId, null);
    }

    // Convert sub points to main points
    public PointTransaction convertSubToMainPoints(String userGoogleId, Integer subPointsSpent, String description) {
        // Calculate main points to receive using current ratio
        Integer mainPointsToReceive = subPointsSpent / PointTransaction.getSubToMainRatio();
        
        if (mainPointsToReceive <= 0) {
            log.warn("Insufficient sub points for conversion. User: {}, Sub points: {}, Ratio: {}", 
                    userGoogleId, subPointsSpent, PointTransaction.getSubToMainRatio());
            return null;
        }

        // Create point transaction
        PointTransaction transaction = description != null 
            ? PointTransaction.createSubToMainConversion(userGoogleId, subPointsSpent, description)
            : PointTransaction.createSubToMainConversion(userGoogleId, subPointsSpent);
        
        // Save transaction as pending
        PointTransaction savedTransaction = pointTransactionRepository.save(transaction);
        
        try {
            // Apply conversion atomically
            boolean success = userPointTokenService.convertSubToMainPoints(userGoogleId, subPointsSpent, mainPointsToReceive);
            
            if (success) {
                // Confirm transaction
                savedTransaction.confirmTransaction();
                pointTransactionRepository.save(savedTransaction);
                log.info("Sub to main conversion successful. User: {}, Sub spent: {}, Main received: {}", 
                        userGoogleId, subPointsSpent, mainPointsToReceive);
            } else {
                // Fail transaction
                savedTransaction.failTransaction();
                pointTransactionRepository.save(savedTransaction);
                log.error("Failed to convert sub to main points. User: {}, Sub points: {}", userGoogleId, subPointsSpent);
            }
        } catch (Exception e) {
            // Fail transaction on exception
            savedTransaction.failTransaction();
            pointTransactionRepository.save(savedTransaction);
            log.error("Exception during sub to main conversion. User: {}, Sub points: {}", userGoogleId, subPointsSpent, e);
        }
        
        return savedTransaction;
    }

    // Convert sub points to main points (without description)
    public PointTransaction convertSubToMainPoints(String userGoogleId, Integer subPointsSpent) {
        return convertSubToMainPoints(userGoogleId, subPointsSpent, null);
    }

    // Exchange main points to tokens
    public PointTransaction exchangeMainPointsToTokens(String userGoogleId, Integer mainPointsSpent, String description) {
        // Calculate tokens to receive using current ratio
        Long tokensToReceive = (long) (mainPointsSpent / PointTransaction.getMainToTokenRatio());
        
        if (tokensToReceive <= 0L) {
            log.warn("Insufficient main points for token exchange. User: {}, Main points: {}, Ratio: {}", 
                    userGoogleId, mainPointsSpent, PointTransaction.getMainToTokenRatio());
            return null;
        }

        // Create point transaction
        PointTransaction transaction = description != null 
            ? PointTransaction.createMainToTokenExchange(userGoogleId, mainPointsSpent, description)
            : PointTransaction.createMainToTokenExchange(userGoogleId, mainPointsSpent);
        
        // Save transaction as pending
        PointTransaction savedTransaction = pointTransactionRepository.save(transaction);
        
        try {
            // Apply exchange atomically
            boolean success = userPointTokenService.exchangeMainPointsToTokens(userGoogleId, mainPointsSpent, tokensToReceive);
            
            if (success) {
                // Confirm transaction
                savedTransaction.confirmTransaction();
                pointTransactionRepository.save(savedTransaction);
                log.info("Main to token exchange successful. User: {}, Main spent: {}, Tokens received: {}", 
                        userGoogleId, mainPointsSpent, tokensToReceive);
            } else {
                // Fail transaction
                savedTransaction.failTransaction();
                pointTransactionRepository.save(savedTransaction);
                log.error("Failed to exchange main points to tokens. User: {}, Main points: {}", userGoogleId, mainPointsSpent);
            }
        } catch (Exception e) {
            // Fail transaction on exception
            savedTransaction.failTransaction();
            pointTransactionRepository.save(savedTransaction);
            log.error("Exception during main to token exchange. User: {}, Main points: {}", userGoogleId, mainPointsSpent, e);
        }
        
        return savedTransaction;
    }

    // Exchange main points to tokens (without description)
    public PointTransaction exchangeMainPointsToTokens(String userGoogleId, Integer mainPointsSpent) {
        return exchangeMainPointsToTokens(userGoogleId, mainPointsSpent, null);
    }

    // Get transaction history for user
    @Transactional(readOnly = true)
    public Page<PointTransaction> getTransactionHistory(String userGoogleId, Pageable pageable) {
        return pointTransactionRepository.findByUserGoogleIdOrderByCreatedAtDesc(userGoogleId, pageable);
    }

    // Get transaction history by point type
    @Transactional(readOnly = true)
    public Page<PointTransaction> getTransactionHistoryByPointType(String userGoogleId, Byte pointTypeId, Pageable pageable) {
        return pointTransactionRepository.findByUserGoogleIdAndPointTypeIdOrderByCreatedAtDesc(userGoogleId, pointTypeId, pageable);
    }

    // Get transaction history by source
    @Transactional(readOnly = true)
    public Page<PointTransaction> getTransactionHistoryBySource(String userGoogleId, Byte sourceId, Pageable pageable) {
        return pointTransactionRepository.findByUserGoogleIdAndSourceIdOrderByCreatedAtDesc(userGoogleId, sourceId, pageable);
    }

    // Get transaction history by status
    @Transactional(readOnly = true)
    public Page<PointTransaction> getTransactionHistoryByStatus(String userGoogleId, Byte transactionStatusId, Pageable pageable) {
        return pointTransactionRepository.findByUserGoogleIdAndTransactionStatusIdOrderByCreatedAtDesc(userGoogleId, transactionStatusId, pageable);
    }

    // Get transaction history by date range
    @Transactional(readOnly = true)
    public Page<PointTransaction> getTransactionHistoryByDateRange(String userGoogleId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return pointTransactionRepository.findByUserGoogleIdAndCreatedAtBetweenOrderByCreatedAtDesc(userGoogleId, startDate, endDate, pageable);
    }

    // Get recent earning transactions
    @Transactional(readOnly = true)
    public List<PointTransaction> getRecentEarningTransactions(String userGoogleId, int limit) {
        Pageable pageable = Pageable.ofSize(limit);
        return pointTransactionRepository.findRecentEarningTransactions(userGoogleId, pageable);
    }

    // Get recent spending transactions
    @Transactional(readOnly = true)
    public List<PointTransaction> getRecentSpendingTransactions(String userGoogleId, int limit) {
        Pageable pageable = Pageable.ofSize(limit);
        return pointTransactionRepository.findRecentSpendingTransactions(userGoogleId, pageable);
    }

    // Get pending transactions
    @Transactional(readOnly = true)
    public List<PointTransaction> getPendingTransactions(String userGoogleId) {
        return pointTransactionRepository.findByUserGoogleIdAndTransactionStatusId(userGoogleId, TransactionStatusType.PENDING.getId());
    }

    // Get transaction by ID
    @Transactional(readOnly = true)
    public Optional<PointTransaction> getTransactionById(Integer transactionId) {
        return pointTransactionRepository.findById(transactionId);
    }

    // Get user statistics
    @Transactional(readOnly = true)
    public UserPointStatistics getUserStatistics(String userGoogleId) {
        Integer totalMainEarned = pointTransactionRepository.getTotalMainPointsEarned(userGoogleId);
        Integer totalSubEarned = pointTransactionRepository.getTotalSubPointsEarned(userGoogleId);
        Integer totalMainExchanged = pointTransactionRepository.getTotalMainPointsExchanged(userGoogleId);
        Integer totalSubConverted = pointTransactionRepository.getTotalSubPointsConverted(userGoogleId);
        long totalTransactions = pointTransactionRepository.countByUserGoogleId(userGoogleId);
        
        return new UserPointStatistics(totalMainEarned, totalSubEarned, totalMainExchanged, totalSubConverted, totalTransactions);
    }

    // Check if user has pending transactions
    @Transactional(readOnly = true)
    public boolean hasPendingTransactions(String userGoogleId) {
        return pointTransactionRepository.existsByUserGoogleIdAndTransactionStatusId(userGoogleId, TransactionStatusType.PENDING.getId());
    }

    // Inner class for user statistics
    public static class UserPointStatistics {
        private final Integer totalMainPointsEarned;
        private final Integer totalSubPointsEarned;
        private final Integer totalMainPointsExchanged;
        private final Integer totalSubPointsConverted;
        private final long totalTransactions;

        public UserPointStatistics(Integer totalMainPointsEarned, Integer totalSubPointsEarned, 
                                 Integer totalMainPointsExchanged, Integer totalSubPointsConverted, long totalTransactions) {
            this.totalMainPointsEarned = totalMainPointsEarned != null ? totalMainPointsEarned : 0;
            this.totalSubPointsEarned = totalSubPointsEarned != null ? totalSubPointsEarned : 0;
            this.totalMainPointsExchanged = totalMainPointsExchanged != null ? totalMainPointsExchanged : 0;
            this.totalSubPointsConverted = totalSubPointsConverted != null ? totalSubPointsConverted : 0;
            this.totalTransactions = totalTransactions;
        }

        // Getters
        public Integer getTotalMainPointsEarned() { return totalMainPointsEarned; }
        public Integer getTotalSubPointsEarned() { return totalSubPointsEarned; }
        public Integer getTotalMainPointsExchanged() { return totalMainPointsExchanged; }
        public Integer getTotalSubPointsConverted() { return totalSubPointsConverted; }
        public long getTotalTransactions() { return totalTransactions; }
    }
}