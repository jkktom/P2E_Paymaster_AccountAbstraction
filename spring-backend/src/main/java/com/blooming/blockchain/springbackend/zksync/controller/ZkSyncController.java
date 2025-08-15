package com.blooming.blockchain.springbackend.zksync.controller;

import com.blooming.blockchain.springbackend.auth.jwt.JwtService;
import com.blooming.blockchain.springbackend.pointtransaction.entity.TokenTransaction;
import com.blooming.blockchain.springbackend.pointtransaction.repository.PointTransactionRepository;
import com.blooming.blockchain.springbackend.pointtransaction.service.PointTransactionService;
import com.blooming.blockchain.springbackend.pointtransaction.service.TokenTransactionService;
import com.blooming.blockchain.springbackend.userdetail.service.UserPointTokenService;
import com.blooming.blockchain.springbackend.zksync.service.ZkSyncService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/zksync")
@RequiredArgsConstructor
@Slf4j
public class ZkSyncController {

    private final ZkSyncService zkSyncService;
    private final JwtService jwtService;
    private final UserPointTokenService userPointTokenService;
    private final TokenTransactionService tokenTransactionService;
    private final PointTransactionService pointTransactionService;
    private final PointTransactionRepository pointTransactionRepository;

    /**
     * Test smart wallet creation (for testing without frontend)
     * @param testEmail Test email for wallet creation
     * @return Created wallet information
     */
    @PostMapping("/test/create-wallet")
    public ResponseEntity<?> testCreateWallet(@RequestParam(defaultValue = "test@example.com") String testEmail) {
        try {
            log.info("Creating test smart wallet for email: {}", testEmail);
            ZkSyncService.SmartWallet smartWallet = zkSyncService.createSmartWallet(testEmail);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("testEmail", smartWallet.getUserEmail());
            response.put("smartWalletAddress", smartWallet.getAddress());
            response.put("privateKeyPreview", smartWallet.getPrivateKey().substring(0, 8) + "...");
            response.put("message", "Smart wallet created successfully for testing");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to create test smart wallet", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to create test smart wallet: " + e.getMessage()));
        }
    }

    /**
     * Test token minting (for testing without frontend)
     * @param mintRequest Request containing wallet address, amount, and reason
     * @return Token minting result with transaction hash
     */
    @PostMapping("/test/mint-tokens")
    public ResponseEntity<?> testMintTokens(@RequestBody Map<String, Object> mintRequest) {
        try {
            String walletAddress = (String) mintRequest.get("walletAddress");
            Object amountObj = mintRequest.get("amount");
            String reason = (String) mintRequest.getOrDefault("reason", "Test token minting");

            if (walletAddress == null || amountObj == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "walletAddress and amount are required"
                ));
            }

            BigInteger amount;
            if (amountObj instanceof Number) {
                amount = BigInteger.valueOf(((Number) amountObj).longValue());
            } else {
                amount = new BigInteger(amountObj.toString());
            }

            log.info("Test minting {} tokens to wallet: {} - Reason: {}", amount, walletAddress, reason);
            
            String txHash = zkSyncService.mintGovernanceTokens(walletAddress, amount, reason).join();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("walletAddress", walletAddress);
            response.put("amount", amount.toString());
            response.put("reason", reason);
            response.put("transactionHash", txHash);
            response.put("explorerUrl", "https://sepolia.era.zksync.dev/tx/" + txHash);
            response.put("message", "Tokens minted successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to mint test tokens", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false, 
                    "error", "Failed to mint tokens: " + e.getMessage()
                ));
        }
    }

    /**
     * Get paymaster status and statistics
     * @return Paymaster information
     */
    @GetMapping("/paymaster/status")
    public ResponseEntity<?> getPaymasterStatus() {
        try {
            ZkSyncService.PaymasterStats stats = zkSyncService.getPaymasterStats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("address", stats.getAddress());
            response.put("balance", stats.getBalance().toString());
            response.put("balanceInEth", formatEthBalance(stats.getBalance()));
            response.put("isActive", stats.isActive());
            response.put("governanceTokenAddress", stats.getGovernanceTokenAddress());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get paymaster status", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to get paymaster status: " + e.getMessage()));
        }
    }

    /**
     * Get wallet balance for authenticated user
     * @param request HTTP request to extract JWT token
     * @return Wallet balance information
     */
    @GetMapping("/wallet/balance")
    public ResponseEntity<?> getWalletBalance(HttpServletRequest request) {
        try {
            String token = extractJwtFromRequest(request);
            if (token == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "JWT token required"));
            }

            String smartWalletAddress = jwtService.extractSmartWalletAddress(token);
            if (smartWalletAddress == null || smartWalletAddress.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Smart wallet address not found in token"));
            }

            BigInteger ethBalance = zkSyncService.getWalletBalance(smartWalletAddress).join();
            BigInteger tokenBalance = zkSyncService.getGovernanceTokenBalance(smartWalletAddress).join();

            Map<String, Object> response = new HashMap<>();
            response.put("address", smartWalletAddress);
            response.put("ethBalance", ethBalance.toString());
            response.put("ethBalanceFormatted", formatEthBalance(ethBalance));
            response.put("governanceTokenBalance", tokenBalance.toString());
            response.put("governanceTokenBalanceFormatted", formatTokenBalance(tokenBalance));

            return ResponseEntity.ok(response);
        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Token expired"));
        } catch (Exception e) {
            log.error("Failed to get wallet balance", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to get wallet balance: " + e.getMessage()));
        }
    }

    /**
     * Exchange exactly 10 main points for 1 governance token
     * @param request HTTP request to extract JWT token  
     * @return Transaction result with database and blockchain updates
     */
    @PostMapping("/exchange/points-to-tokens")
    @Transactional
    public ResponseEntity<?> exchangePointsToTokens(HttpServletRequest request) {
        try {
            String token = extractJwtFromRequest(request);
            if (token == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "JWT token required"));
            }

            String googleId = jwtService.extractGoogleId(token);
            String smartWalletAddress = jwtService.extractSmartWalletAddress(token);
            if (smartWalletAddress == null || smartWalletAddress.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Smart wallet address not found in token"));
            }

            // Fixed exchange: 10 main points → 1 token
            final Integer MAIN_POINTS_TO_EXCHANGE = 10;
            final Integer TOKENS_TO_RECEIVE = 1;
            final BigInteger TOKEN_AMOUNT_WEI = BigInteger.valueOf(1).multiply(BigInteger.valueOf(10).pow(18)); // 1 token = 1e18 wei

            log.info("Exchange request - User: {}, Wallet: {}, 10 main points → 1 token", googleId, smartWalletAddress);

            // Check user's current main point balance
            Integer currentMainPoints = userPointTokenService.getMainPointBalance(googleId);
            if (currentMainPoints < MAIN_POINTS_TO_EXCHANGE) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", String.format("Insufficient main points. You have %d, need 10 for exchange", currentMainPoints),
                    "currentMainPoints", currentMainPoints,
                    "requiredMainPoints", MAIN_POINTS_TO_EXCHANGE
                ));
            }

            // Step 1: Create token transaction record
            TokenTransaction tokenTransaction = tokenTransactionService.createTokenTransaction(
                googleId, 
                MAIN_POINTS_TO_EXCHANGE, 
                TOKEN_AMOUNT_WEI.longValue(),
                "Exchange: 10 main points -> 1 BLOOM token"
            );

            // Step 2: Update database (deduct 10 main points, add 1 token worth in wei)
            boolean dbUpdateSuccess = userPointTokenService.exchangeMainPointsToTokens(
                googleId, MAIN_POINTS_TO_EXCHANGE, TOKEN_AMOUNT_WEI.longValue());
            
            if (!dbUpdateSuccess) {
                // Mark transaction as failed
                tokenTransactionService.failTokenTransaction(tokenTransaction.getId());
                return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", "Failed to update database balances"
                ));
            }

            log.info("Database updated successfully - deducted {} main points, added {} token balance for user: {}", 
                MAIN_POINTS_TO_EXCHANGE, TOKEN_AMOUNT_WEI, googleId);

            // Step 2.5: Record point spending transaction (SOURCE_ID = 5 = MAIN_EXCHANGE)
            try {
                com.blooming.blockchain.springbackend.pointtransaction.entity.PointTransaction pointTx = 
                    com.blooming.blockchain.springbackend.pointtransaction.entity.PointTransaction.createMainToTokenExchange(
                        googleId, MAIN_POINTS_TO_EXCHANGE, "Exchange: 10 main points -> 1 BLOOM token");
                pointTx.confirmTransaction(); // Mark as confirmed since balance update succeeded
                pointTransactionRepository.save(pointTx);
                log.info("Recorded point spending transaction for user: {} - {} main points spent", googleId, MAIN_POINTS_TO_EXCHANGE);
            } catch (Exception e) {
                log.warn("Failed to record point spending transaction, but continuing with token minting: {}", e.getMessage());
                // Don't fail the entire exchange if logging fails
            }

            // Step 3: Mint 1 actual token on blockchain
            String txHash;
            try {
                txHash = zkSyncService.mintGovernanceTokens(
                    smartWalletAddress, 
                    TOKEN_AMOUNT_WEI, 
                    "Exchange: 10 main points -> 1 BLOOM token"
                ).join();
                
                // Confirm the token transaction with blockchain hash
                tokenTransactionService.confirmTokenTransaction(tokenTransaction.getId(), txHash);
                log.info("Successfully completed full exchange - User: {}, TX: {}", googleId, txHash);
                
            } catch (Exception e) {
                // If blockchain minting fails, rollback database changes
                log.error("Blockchain minting failed, rolling back database changes for user: {}", googleId, e);
                
                // Mark transaction as failed
                tokenTransactionService.failTokenTransaction(tokenTransaction.getId());
                
                // Rollback: add back 10 main points
                userPointTokenService.addMainPoints(googleId, MAIN_POINTS_TO_EXCHANGE);
                
                return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", "Blockchain transaction failed, changes rolled back: " + e.getMessage()
                ));
            }

            // Get updated balances for response
            Integer newMainPointBalance = userPointTokenService.getMainPointBalance(googleId);
            Long newTokenBalance = userPointTokenService.getTokenBalance(googleId);

            // Return success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mainPointsExchanged", MAIN_POINTS_TO_EXCHANGE);
            response.put("tokensReceived", TOKENS_TO_RECEIVE);
            response.put("walletAddress", smartWalletAddress);
            response.put("transactionHash", txHash);
            response.put("explorerUrl", "https://sepolia.era.zksync.dev/tx/" + txHash);
            response.put("tokenTransactionId", tokenTransaction.getId());
            response.put("newMainPointBalance", newMainPointBalance);
            response.put("newTokenBalance", newTokenBalance);
            response.put("newTokenBalanceFormatted", formatTokenBalance(BigInteger.valueOf(newTokenBalance)));
            response.put("message", "Successfully exchanged 10 main points for 1 BLOOM token");

            return ResponseEntity.ok(response);
            
        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Token expired"));
        } catch (Exception e) {
            log.error("Failed to perform points to tokens exchange", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to exchange points to tokens: " + e.getMessage()));
        }
    }

    // Helper methods

    private String extractJwtFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private String formatEthBalance(BigInteger weiBalance) {
        if (weiBalance.equals(BigInteger.ZERO)) {
            return "0.0 ETH";
        }
        
        // Convert Wei to ETH (divide by 10^18)
        BigInteger ethUnit = new BigInteger("1000000000000000000"); // 10^18
        BigInteger ethWhole = weiBalance.divide(ethUnit);
        BigInteger ethRemainder = weiBalance.remainder(ethUnit);
        
        // Format to 4 decimal places
        double ethValue = ethWhole.doubleValue() + (ethRemainder.doubleValue() / ethUnit.doubleValue());
        return String.format("%.4f ETH", ethValue);
    }

    private String formatTokenBalance(BigInteger tokenBalance) {
        if (tokenBalance.equals(BigInteger.ZERO)) {
            return "0.0 BLOOM";
        }
        
        // Assuming 18 decimals for governance token
        BigInteger tokenUnit = new BigInteger("1000000000000000000"); // 10^18
        BigInteger tokenWhole = tokenBalance.divide(tokenUnit);
        BigInteger tokenRemainder = tokenBalance.remainder(tokenUnit);
        
        double tokenValue = tokenWhole.doubleValue() + (tokenRemainder.doubleValue() / tokenUnit.doubleValue());
        return String.format("%.2f BLOOM", tokenValue);
    }

    private String generateMockTxHash() {
        // Generate a realistic-looking transaction hash for testing
        return "abcd1234567890abcdef1234567890abcdef1234567890abcdef1234567890ab";
    }
}