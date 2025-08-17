package com.blooming.blockchain.springbackend.zksync.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import com.blooming.blockchain.springbackend.zksync.dto.CreateProposalResult;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@Slf4j
public class ZkSyncService {

    @Value("${app.zksync.rpc-url:https://sepolia.era.zksync.dev}")
    private String zkSyncRpcUrl;

    @Value("${app.zksync.governance.token.address:0x21341E1672ee0A7ddADB5D7BFF72F93C8E81EF3e}")
    private String governanceTokenAddress;

    @Value("${app.zksync.paymaster.address:0x10219E515c3955916d79A1aC614B86187f0872BC}")
    private String paymasterAddress;

    @Value("${app.zksync.chain-id:300}")
    private Integer chainId;

    @Value("${app.zksync.owner.private-key:your-owner-private-key-here}")
    private String ownerPrivateKey;

    private Web3j web3j;
    private Credentials ownerCredentials;

    public ZkSyncService() {
        // Initialize Web3j client lazily
    }

    private Web3j getWeb3jClient() {
        if (web3j == null) {
            web3j = Web3j.build(new HttpService(zkSyncRpcUrl));
        }
        return web3j;
    }

    private Credentials getOwnerCredentials() {
        if (ownerCredentials == null) {
            if (ownerPrivateKey == null || ownerPrivateKey.equals("your-owner-private-key-here")) {
                throw new RuntimeException("Owner private key not configured. Set ZKSYNC_OWNER_PRIVATE_KEY environment variable.");
            }
            try {
                // Remove 0x prefix if present
                String cleanPrivateKey = ownerPrivateKey.startsWith("0x") ? 
                    ownerPrivateKey.substring(2) : ownerPrivateKey;
                ownerCredentials = Credentials.create(cleanPrivateKey);
                log.info("Owner credentials initialized for address: {}", ownerCredentials.getAddress());
            } catch (Exception e) {
                throw new RuntimeException("Failed to create owner credentials from private key", e);
            }
        }
        return ownerCredentials;
    }

    /**
     * Create a new smart wallet for a user
     * @param userEmail User's email for logging purposes
     * @return SmartWallet object containing address and private key
     */
    public SmartWallet createSmartWallet(String userEmail) {
        try {
            log.info("Creating smart wallet for user: {}", userEmail);
            
            // Generate new private key securely
            SecureRandom secureRandom = new SecureRandom();
            BigInteger privateKeyBigInt = new BigInteger(256, secureRandom);
            
            // Ensure private key is valid (not zero, within curve order)
            while (privateKeyBigInt.equals(BigInteger.ZERO) || 
                   privateKeyBigInt.compareTo(new BigInteger("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141", 16)) >= 0) {
                privateKeyBigInt = new BigInteger(256, secureRandom);
            }
            
            String privateKey = privateKeyBigInt.toString(16);
            
            // Generate credentials and address
            Credentials credentials = Credentials.create(privateKey);
            String walletAddress = credentials.getAddress();
            
            log.info("Created smart wallet for user {}: {}", userEmail, walletAddress);
            
            return SmartWallet.builder()
                .address(walletAddress)
                .privateKey(privateKey)
                .userEmail(userEmail)
                .build();
                
        } catch (Exception e) {
            log.error("Failed to create smart wallet for user: {}", userEmail, e);
            throw new RuntimeException("Smart wallet creation failed", e);
        }
    }

    /**
     * Get wallet balance in ETH
     * @param walletAddress Wallet address
     * @return Balance in Wei as BigInteger
     */
    public CompletableFuture<BigInteger> getWalletBalance(String walletAddress) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Web3j client = getWeb3jClient();
                return client.ethGetBalance(walletAddress, DefaultBlockParameterName.LATEST)
                    .send()
                    .getBalance();
            } catch (Exception e) {
                log.error("Failed to get wallet balance for address: {}", walletAddress, e);
                return BigInteger.ZERO;
            }
        });
    }

    /**
     * Get governance token balance for a wallet
     * @param walletAddress Wallet address
     * @return Token balance as BigInteger
     */
    public CompletableFuture<BigInteger> getGovernanceTokenBalance(String walletAddress) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // This would require implementing ERC20 contract interaction
                // For now, return zero - will implement in next phase
                log.debug("Getting governance token balance for: {}", walletAddress);
                return BigInteger.ZERO;
            } catch (Exception e) {
                log.error("Failed to get governance token balance for address: {}", walletAddress, e);
                return BigInteger.ZERO;
            }
        });
    }

    /**
     * Execute gasless transaction using paymaster (mock implementation)
     * @param userPrivateKey User's private key
     * @param contractAddress Target contract address
     * @param functionData Encoded function call data
     * @return Transaction hash
     */
    public CompletableFuture<String> executeGaslessTransaction(
            String userPrivateKey, 
            String contractAddress, 
            String functionData) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Executing gasless transaction to contract: {}", contractAddress);
                
                // This is a placeholder for the actual gasless transaction implementation
                // In production, this would integrate with zkSync's paymaster flow
                log.info("Mock gasless transaction executed - UserKey: {}, Contract: {}", 
                    userPrivateKey.substring(0, 8) + "...", contractAddress);
                
                // Return mock transaction hash for now
                return "0x" + generateMockTxHash();
                
            } catch (Exception e) {
                log.error("Failed to execute gasless transaction", e);
                throw new RuntimeException("Gasless transaction failed", e);
            }
        });
    }

    /**
     * Mint governance tokens using backend owner credentials
     * @param userWalletAddress User's wallet address to receive tokens
     * @param amount Amount of tokens to mint (in wei, 18 decimals)
     * @param reason Reason for minting (audit trail)
     * @return Transaction hash
     */
    public CompletableFuture<String> mintGovernanceTokens(
            String userWalletAddress, 
            BigInteger amount,
            String reason) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Minting {} governance tokens for wallet: {} - Reason: {}", amount, userWalletAddress, reason);
                
                Web3j web3j = getWeb3jClient();
                Credentials ownerCreds = getOwnerCredentials();
                
                // Create the mintForExchange function call
                Function mintFunction = new Function(
                    "mintForExchange",
                    Arrays.asList(
                        new Address(userWalletAddress),
                        new Uint256(amount),
                        new Utf8String(reason)
                    ),
                    Collections.emptyList()
                );
                
                // Encode the function call
                String encodedFunction = FunctionEncoder.encode(mintFunction);
                
                // Create transaction manager
                RawTransactionManager transactionManager = new RawTransactionManager(
                    web3j, ownerCreds, chainId.longValue());
                
                // Send transaction
                org.web3j.protocol.core.methods.response.EthSendTransaction ethSendTransaction = 
                    transactionManager.sendTransaction(
                        DefaultGasProvider.GAS_PRICE,
                        DefaultGasProvider.GAS_LIMIT,
                        governanceTokenAddress,
                        encodedFunction,
                        BigInteger.ZERO
                    );
                
                if (ethSendTransaction.hasError()) {
                    throw new RuntimeException("Transaction failed: " + ethSendTransaction.getError().getMessage());
                }
                
                String txHash = ethSendTransaction.getTransactionHash();
                log.info("Successfully sent mint transaction for {} tokens to {} - TX: {}", amount, userWalletAddress, txHash);
                
                // Wait for transaction receipt (optional - for confirmation)
                try {
                    // Poll for transaction receipt
                    TransactionReceipt receipt = null;
                    int attempts = 0;
                    int maxAttempts = 30; // Wait up to 30 seconds
                    
                    while (receipt == null && attempts < maxAttempts) {
                        Thread.sleep(1000); // Wait 1 second
                        var receiptResponse = web3j.ethGetTransactionReceipt(txHash).send();
                        if (receiptResponse.getTransactionReceipt().isPresent()) {
                            receipt = receiptResponse.getTransactionReceipt().get();
                            break;
                        }
                        attempts++;
                    }
                    
                    if (receipt != null) {
                        if (receipt.isStatusOK()) {
                            log.info("Transaction confirmed successfully - TX: {}", txHash);
                        } else {
                            log.warn("Transaction completed but may have failed - TX: {}, Status: {}", txHash, receipt.getStatus());
                        }
                    } else {
                        log.warn("Transaction receipt not found after {} attempts - TX: {}", maxAttempts, txHash);
                    }
                } catch (Exception e) {
                    log.warn("Could not get transaction receipt, but transaction was sent - TX: {} - Error: {}", txHash, e.getMessage());
                }
                
                return txHash;
                
            } catch (Exception e) {
                log.error("Failed to mint governance tokens for wallet: {}", userWalletAddress, e);
                throw new RuntimeException("Token minting failed: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Mint governance tokens using gasless transaction (DEPRECATED - use mintGovernanceTokens instead)
     * @param userWalletAddress User's wallet address
     * @param userPrivateKey User's private key  
     * @param amount Amount of tokens to mint
     * @return Transaction hash
     */
    @Deprecated
    public CompletableFuture<String> mintGovernanceTokensGasless(
            String userWalletAddress, 
            String userPrivateKey, 
            BigInteger amount) {
        
        // Redirect to new implementation with default reason
        return mintGovernanceTokens(userWalletAddress, amount, "User signup token grant");
    }

    /**
     * Check if paymaster has sufficient balance
     * @return true if paymaster can sponsor transactions
     */
    public boolean isPaymasterFunded() {
        try {
            BigInteger paymasterBalance = getWalletBalance(paymasterAddress).join();
            BigInteger minimumBalance = new BigInteger("1000000000000000"); // 0.001 ETH in Wei
            
            boolean isFunded = paymasterBalance.compareTo(minimumBalance) >= 0;
            log.info("Paymaster balance check - Balance: {} Wei, Funded: {}", paymasterBalance, isFunded);
            
            return isFunded;
        } catch (Exception e) {
            log.error("Failed to check paymaster balance", e);
            return false;
        }
    }

    /**
     * Get paymaster statistics
     * @return PaymasterStats object with current information
     */
    public PaymasterStats getPaymasterStats() {
        try {
            BigInteger balance = getWalletBalance(paymasterAddress).join();
            boolean isActive = isPaymasterFunded();
            
            return PaymasterStats.builder()
                .address(paymasterAddress)
                .balance(balance)
                .isActive(isActive)
                .governanceTokenAddress(governanceTokenAddress)
                .build();
                
        } catch (Exception e) {
            log.error("Failed to get paymaster stats", e);
            return PaymasterStats.builder()
                .address(paymasterAddress)
                .balance(BigInteger.ZERO)
                .isActive(false)
                .governanceTokenAddress(governanceTokenAddress)
                .build();
        }
    }

    // Helper methods
    
    private String encodeMintFunction(String recipient, BigInteger amount) {
        // This would use Web3j's contract wrapper to encode the mint function call
        // For now, return a placeholder
        return "0xa0712d68" + // mint(address,uint256) function selector
               padAddress(recipient) + 
               padUint256(amount);
    }

    private String padAddress(String address) {
        // Remove 0x prefix if present and pad to 64 characters
        String cleanAddress = address.startsWith("0x") ? address.substring(2) : address;
        return String.format("%064s", cleanAddress).replace(' ', '0');
    }

    private String padUint256(BigInteger value) {
        // Convert to hex and pad to 64 characters
        String hex = value.toString(16);
        return String.format("%064s", hex).replace(' ', '0');
    }

    private String generateMockTxHash() {
        // Generate a realistic-looking transaction hash for testing
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // Inner classes for data transfer

    @lombok.Builder
    @lombok.Data
    public static class SmartWallet {
        private String address;
        private String privateKey;
        private String userEmail;
    }

    @lombok.Builder
    @lombok.Data
    public static class PaymasterStats {
        private String address;
        private BigInteger balance;
        private boolean isActive;
        private String governanceTokenAddress;
    }
}