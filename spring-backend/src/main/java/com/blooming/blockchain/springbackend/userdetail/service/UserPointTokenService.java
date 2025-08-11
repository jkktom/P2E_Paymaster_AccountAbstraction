package com.blooming.blockchain.springbackend.userdetail.service;

import com.blooming.blockchain.springbackend.userdetail.entity.UserPointToken;
import com.blooming.blockchain.springbackend.userdetail.repository.UserPointTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserPointTokenService {

    private final UserPointTokenRepository userPointTokenRepository;

    // Get or create user balance record
    public UserPointToken getOrCreateUserBalance(String userGoogleId) {
        return userPointTokenRepository.findByUserGoogleId(userGoogleId)
                .orElseGet(() -> {
                    UserPointToken newBalance = new UserPointToken(userGoogleId, 0, 0, 0L);
                    return userPointTokenRepository.save(newBalance);
                });
    }

    // Get user balance (read-only)
    @Transactional(readOnly = true)
    public Optional<UserPointToken> getUserBalance(String userGoogleId) {
        return userPointTokenRepository.findByUserGoogleId(userGoogleId);
    }

    // Add main points to user balance
    public boolean addMainPoints(String userGoogleId, Integer amount) {
        if (amount <= 0) {
            log.warn("Attempt to add non-positive main points: {} for user: {}", amount, userGoogleId);
            return false;
        }

        // Ensure user balance record exists
        getOrCreateUserBalance(userGoogleId);
        
        int updatedRows = userPointTokenRepository.addMainPoints(userGoogleId, amount);
        if (updatedRows > 0) {
            log.info("Added {} main points to user: {}", amount, userGoogleId);
            return true;
        }
        
        log.error("Failed to add main points to user: {}", userGoogleId);
        return false;
    }

    // Add sub points to user balance
    public boolean addSubPoints(String userGoogleId, Integer amount) {
        if (amount <= 0) {
            log.warn("Attempt to add non-positive sub points: {} for user: {}", amount, userGoogleId);
            return false;
        }

        // Ensure user balance record exists
        getOrCreateUserBalance(userGoogleId);
        
        int updatedRows = userPointTokenRepository.addSubPoints(userGoogleId, amount);
        if (updatedRows > 0) {
            log.info("Added {} sub points to user: {}", amount, userGoogleId);
            return true;
        }
        
        log.error("Failed to add sub points to user: {}", userGoogleId);
        return false;
    }

    // Spend main points (with balance validation)
    public boolean spendMainPoints(String userGoogleId, Integer amount) {
        if (amount <= 0) {
            log.warn("Attempt to spend non-positive main points: {} for user: {}", amount, userGoogleId);
            return false;
        }

        // Check if user has sufficient balance
        Optional<Integer> currentBalance = userPointTokenRepository.getMainPointBalance(userGoogleId);
        if (currentBalance.isEmpty() || currentBalance.get() < amount) {
            log.warn("Insufficient main points balance for user: {}. Required: {}, Available: {}", 
                    userGoogleId, amount, currentBalance.orElse(0));
            return false;
        }

        int updatedRows = userPointTokenRepository.spendMainPoints(userGoogleId, amount);
        if (updatedRows > 0) {
            log.info("Spent {} main points for user: {}", amount, userGoogleId);
            return true;
        }
        
        log.error("Failed to spend main points for user: {}", userGoogleId);
        return false;
    }

    // Spend sub points (with balance validation)
    public boolean spendSubPoints(String userGoogleId, Integer amount) {
        if (amount <= 0) {
            log.warn("Attempt to spend non-positive sub points: {} for user: {}", amount, userGoogleId);
            return false;
        }

        // Check if user has sufficient balance
        Optional<Integer> currentBalance = userPointTokenRepository.getSubPointBalance(userGoogleId);
        if (currentBalance.isEmpty() || currentBalance.get() < amount) {
            log.warn("Insufficient sub points balance for user: {}. Required: {}, Available: {}", 
                    userGoogleId, amount, currentBalance.orElse(0));
            return false;
        }

        int updatedRows = userPointTokenRepository.spendSubPoints(userGoogleId, amount);
        if (updatedRows > 0) {
            log.info("Spent {} sub points for user: {}", amount, userGoogleId);
            return true;
        }
        
        log.error("Failed to spend sub points for user: {}", userGoogleId);
        return false;
    }

    // Add tokens to user balance
    public boolean addTokens(String userGoogleId, Long amount) {
        if (amount <= 0L) {
            log.warn("Attempt to add non-positive tokens: {} for user: {}", amount, userGoogleId);
            return false;
        }

        // Ensure user balance record exists
        getOrCreateUserBalance(userGoogleId);
        
        int updatedRows = userPointTokenRepository.addTokens(userGoogleId, amount);
        if (updatedRows > 0) {
            log.info("Added {} tokens to user: {}", amount, userGoogleId);
            return true;
        }
        
        log.error("Failed to add tokens to user: {}", userGoogleId);
        return false;
    }

    // Convert sub points to main points atomically
    public boolean convertSubToMainPoints(String userGoogleId, Integer subAmount, Integer mainAmount) {
        if (subAmount <= 0 || mainAmount <= 0) {
            log.warn("Invalid conversion amounts - sub: {}, main: {} for user: {}", subAmount, mainAmount, userGoogleId);
            return false;
        }

        // Check if user has sufficient sub points
        Optional<Integer> currentSubBalance = userPointTokenRepository.getSubPointBalance(userGoogleId);
        if (currentSubBalance.isEmpty() || currentSubBalance.get() < subAmount) {
            log.warn("Insufficient sub points for conversion. User: {}, Required: {}, Available: {}", 
                    userGoogleId, subAmount, currentSubBalance.orElse(0));
            return false;
        }

        int updatedRows = userPointTokenRepository.convertSubToMainPoints(userGoogleId, subAmount, mainAmount);
        if (updatedRows > 0) {
            log.info("Converted {} sub points to {} main points for user: {}", subAmount, mainAmount, userGoogleId);
            return true;
        }
        
        log.error("Failed to convert points for user: {}", userGoogleId);
        return false;
    }

    // Exchange main points to tokens atomically
    public boolean exchangeMainPointsToTokens(String userGoogleId, Integer mainAmount, Long tokenAmount) {
        if (mainAmount <= 0 || tokenAmount <= 0L) {
            log.warn("Invalid exchange amounts - main: {}, tokens: {} for user: {}", mainAmount, tokenAmount, userGoogleId);
            return false;
        }

        // Check if user has sufficient main points
        Optional<Integer> currentMainBalance = userPointTokenRepository.getMainPointBalance(userGoogleId);
        if (currentMainBalance.isEmpty() || currentMainBalance.get() < mainAmount) {
            log.warn("Insufficient main points for exchange. User: {}, Required: {}, Available: {}", 
                    userGoogleId, mainAmount, currentMainBalance.orElse(0));
            return false;
        }

        int updatedRows = userPointTokenRepository.exchangeMainPointsToTokens(userGoogleId, mainAmount, tokenAmount);
        if (updatedRows > 0) {
            log.info("Exchanged {} main points to {} tokens for user: {}", mainAmount, tokenAmount, userGoogleId);
            return true;
        }
        
        log.error("Failed to exchange points to tokens for user: {}", userGoogleId);
        return false;
    }

    // Get individual balance amounts (read-only methods)
    @Transactional(readOnly = true)
    public Integer getMainPointBalance(String userGoogleId) {
        return userPointTokenRepository.getMainPointBalance(userGoogleId).orElse(0);
    }

    @Transactional(readOnly = true)
    public Integer getSubPointBalance(String userGoogleId) {
        return userPointTokenRepository.getSubPointBalance(userGoogleId).orElse(0);
    }

    @Transactional(readOnly = true)
    public Long getTokenBalance(String userGoogleId) {
        return userPointTokenRepository.getTokenBalance(userGoogleId).orElse(0L);
    }

    // Check if user has sufficient balance
    @Transactional(readOnly = true)
    public boolean hasSufficientMainPoints(String userGoogleId, Integer requiredAmount) {
        return getMainPointBalance(userGoogleId) >= requiredAmount;
    }

    @Transactional(readOnly = true)
    public boolean hasSufficientSubPoints(String userGoogleId, Integer requiredAmount) {
        return getSubPointBalance(userGoogleId) >= requiredAmount;
    }

    // Check if user balance record exists
    @Transactional(readOnly = true)
    public boolean userBalanceExists(String userGoogleId) {
        return userPointTokenRepository.existsByUserGoogleId(userGoogleId);
    }
}