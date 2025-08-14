package com.blooming.blockchain.springbackend.zksync.controller;

import com.blooming.blockchain.springbackend.auth.jwt.JwtService;
import com.blooming.blockchain.springbackend.zksync.service.ZkSyncService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
     * Test endpoint for gasless token exchange
     * @param request HTTP request to extract JWT token  
     * @param exchangeRequest Exchange request data
     * @return Transaction result
     */
    @PostMapping("/exchange/gasless")
    public ResponseEntity<?> performGaslessExchange(
            HttpServletRequest request,
            @RequestBody Map<String, Object> exchangeRequest) {
        try {
            String token = extractJwtFromRequest(request);
            if (token == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "JWT token required"));
            }

            String smartWalletAddress = jwtService.extractSmartWalletAddress(token);
            if (smartWalletAddress == null || smartWalletAddress.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Smart wallet address not found in token"));
            }

            // Extract amount from request
            Object amountObj = exchangeRequest.get("amount");
            if (amountObj == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Amount is required"));
            }

            BigInteger amount;
            if (amountObj instanceof Number) {
                amount = BigInteger.valueOf(((Number) amountObj).longValue());
            } else {
                amount = new BigInteger(amountObj.toString());
            }

            // For now, return a mock response since we haven't implemented the full gasless flow yet
            log.info("Gasless exchange requested for wallet {}: {} tokens", smartWalletAddress, amount);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Gasless exchange initiated");
            response.put("walletAddress", smartWalletAddress);
            response.put("amount", amount.toString());
            response.put("txHash", "0x" + generateMockTxHash());
            response.put("status", "pending");

            return ResponseEntity.ok(response);
        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Token expired"));
        } catch (Exception e) {
            log.error("Failed to perform gasless exchange", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to perform gasless exchange: " + e.getMessage()));
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