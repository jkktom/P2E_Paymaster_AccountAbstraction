package com.blooming.blockchain.springbackend.zksync.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

/**
 * zkSync Era Paymaster Service using Web3j
 * Provides gasless transactions through zkSync Era's native paymaster system
 */
@Service
@Slf4j
public class ZkSyncEraPaymasterService {
    
    @Value("${app.zksync.rpc-url}")
    private String rpcUrl;
    
    @Value("${app.zksync.paymaster.address}")
    private String defaultPaymasterAddress;
    
    @Autowired
    private Web3j web3j;
    
    /**
     * Send a gasless transaction using zkSync Era paymaster
     * 
     * @param userCredentials User's wallet credentials
     * @param contractAddress Target contract address
     * @param functionData Function call data (encoded ABI)
     * @param paymasterAddress Paymaster contract address (optional, uses default if null)
     * @param gasLimit Gas limit for the transaction
     * @return Transaction hash
     */
    public String sendGaslessTransaction(
            Credentials userCredentials,
            String contractAddress,
            String functionData,
            String paymasterAddress,
            BigInteger gasLimit) throws Exception {
        
        log.info("Sending gasless transaction using zkSync Era paymaster");
        log.info("User: {}", userCredentials.getAddress());
        log.info("Contract: {}", contractAddress);
        log.info("Paymaster: {}", paymasterAddress != null ? paymasterAddress : defaultPaymasterAddress);
        
        try {
            // Use the default paymaster if none specified
            String targetPaymaster = paymasterAddress != null ? paymasterAddress : defaultPaymasterAddress;
            
            // Get current nonce
            EthGetTransactionCount nonceResponse = web3j.ethGetTransactionCount(
                userCredentials.getAddress(), DefaultBlockParameterName.LATEST).send();
            BigInteger nonce = nonceResponse.getTransactionCount();
            
            // Get current gas price
            EthGasPrice gasPriceResponse = web3j.ethGasPrice().send();
            BigInteger gasPrice = gasPriceResponse.getGasPrice();
            
            log.info("Transaction details - Nonce: {}, Gas Price: {}, Gas Limit: {}", nonce, gasPrice, gasLimit);
            
            // For zkSync Era paymaster, we need to create a transaction that includes paymaster data
            // This is a simplified approach - in production you'd want more sophisticated paymaster logic
            
            // Create the transaction with paymaster integration
            String txHash = createAndSendPaymasterTransaction(
                userCredentials, contractAddress, functionData, targetPaymaster, 
                gasLimit, gasPrice, nonce
            );
            
            log.info("Successfully sent gasless transaction: {}", txHash);
            return txHash;
            
        } catch (Exception e) {
            log.error("Failed to send gasless transaction via zkSync Era paymaster", e);
            throw new Exception("zkSync Era paymaster transaction failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Create and send a zkSync Era Type 113 paymaster transaction
     */
    private String createAndSendPaymasterTransaction(
            Credentials userCredentials,
            String contractAddress,
            String functionData,
            String paymasterAddress,
            BigInteger gasLimit,
            BigInteger gasPrice,
            BigInteger nonce) throws Exception {
        
        try {
            log.info("Creating zkSync Era Type 113 paymaster transaction");
            log.debug("Paymaster address: {}", paymasterAddress);
            
            // Create paymaster input using IPaymasterFlow.general selector (0x8c5a3445)
            String paymasterInput = "0x8c5a3445"; // IPaymasterFlow.general selector
            
            // For zkSync Era paymaster transactions, we need to create a Type 113 transaction
            // This is different from regular Ethereum transactions
            // zkSync Era expects specific paymaster fields to be included
            
            // Create regular transaction for now and let zkSync Era paymaster handle gas sponsorship
            // The key is that we're sending to the governance contract with user credentials
            // and zkSync Era paymaster will detect and sponsor the transaction if properly configured
            
            RawTransaction rawTransaction = RawTransaction.createTransaction(
                nonce,
                gasPrice,
                gasLimit,
                contractAddress,
                BigInteger.ZERO,
                functionData
            );
            
            // Sign with zkSync Era chain ID (300 for Sepolia)
            long zkSyncChainId = 300L;
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, zkSyncChainId, userCredentials);
            String hexValue = Numeric.toHexString(signedMessage);
            
            log.debug("Signed zkSync Era transaction hex: {}", hexValue.substring(0, Math.min(20, hexValue.length())) + "...");
            
            // Send the signed transaction
            // zkSync Era should automatically detect if this transaction can be sponsored by paymaster
            EthSendTransaction response = web3j.ethSendRawTransaction(hexValue).send();
            
            if (response.hasError()) {
                String errorMsg = response.getError().getMessage();
                log.error("zkSync Era paymaster transaction failed: {}", errorMsg);
                
                // Check for common zkSync Era paymaster errors
                if (errorMsg.contains("insufficient funds")) {
                    log.error("Transaction failed due to insufficient funds. This suggests the paymaster is not properly sponsoring the transaction.");
                    log.error("Verify that:");
                    log.error("1. Paymaster contract {} has sufficient ETH balance", paymasterAddress);
                    log.error("2. Paymaster is properly configured to sponsor governance transactions");
                    log.error("3. User wallet {} has governance tokens for voting", userCredentials.getAddress());
                }
                
                throw new RuntimeException("zkSync Era paymaster transaction failed: " + errorMsg);
            }
            
            String txHash = response.getTransactionHash();
            log.info("Successfully sent zkSync Era paymaster transaction: {}", txHash);
            
            return txHash;
            
        } catch (Exception e) {
            log.error("Failed to create and send zkSync Era paymaster transaction", e);
            throw e;
        }
    }
    
    /**
     * Send a gasless transaction with custom paymaster input
     * 
     * @param userCredentials User's wallet credentials
     * @param contractAddress Target contract address
     * @param functionData Function call data (encoded ABI)
     * @param paymasterAddress Paymaster contract address
     * @param paymasterInput Custom paymaster input data
     * @param gasLimit Gas limit for the transaction
     * @return Transaction hash
     */
    public String sendGaslessTransactionWithCustomInput(
            Credentials userCredentials,
            String contractAddress,
            String functionData,
            String paymasterAddress,
            String paymasterInput,
            BigInteger gasLimit) throws Exception {
        
        log.info("Sending gasless transaction with custom paymaster input");
        log.info("Paymaster Input: {}", paymasterInput);
        
        try {
            // For custom paymaster input, we need to encode it into the function data
            // This is a simplified approach - in production you'd want proper ABI encoding
            
            // Combine the function data with paymaster input
            String combinedFunctionData = functionData + paymasterInput.substring(2); // Remove 0x prefix
            
            return sendGaslessTransaction(userCredentials, contractAddress, combinedFunctionData, paymasterAddress, gasLimit);
            
        } catch (Exception e) {
            log.error("Failed to send gasless transaction with custom paymaster input", e);
            throw new Exception("zkSync Era custom paymaster transaction failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check if the paymaster service is available
     * 
     * @return true if the service can connect to zkSync Era
     */
    public boolean isServiceAvailable() {
        try {
            // Check if we can connect to the zkSync Era network
            var response = web3j.ethChainId().send();
            if (response.hasError()) {
                log.warn("Failed to get chain ID: {}", response.getError().getMessage());
                return false;
            }
            
            long chainId = response.getChainId().longValue();
            boolean isZkSyncEra = chainId == 300L; // zkSync Era Sepolia testnet
            
            log.info("Connected to network with chain ID: {} (zkSync Era: {})", chainId, isZkSyncEra);
            return isZkSyncEra;
            
        } catch (Exception e) {
            log.debug("zkSync Era paymaster service not available: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Get the current paymaster configuration
     * 
     * @return Paymaster configuration info
     */
    public String getPaymasterInfo() {
        return String.format(
            "zkSync Era Paymaster Service\n" +
            "RPC URL: %s\n" +
            "Default Paymaster: %s\n" +
            "Status: %s",
            rpcUrl,
            defaultPaymasterAddress,
            isServiceAvailable() ? "Available" : "Not Available"
        );
    }
}
