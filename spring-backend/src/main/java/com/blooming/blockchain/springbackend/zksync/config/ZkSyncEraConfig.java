package com.blooming.blockchain.springbackend.zksync.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

/**
 * Configuration class for zkSync Era integration
 * Sets up Web3j client and prepares for zkSync Era SDK integration
 */
@Configuration
@Slf4j
public class ZkSyncEraConfig {
    
    @Value("${app.zksync.rpc-url:https://sepolia.era.zksync.dev}")
    private String rpcUrl;
    
    @Value("${app.zksync.paymaster.address}")
    private String paymasterAddress;
    
    /**
     * Configure Web3j client for zkSync Era
     */
    @Bean
    public Web3j zkSyncEraWeb3j() {
        log.info("Initializing zkSync Era Web3j client with RPC: {}", rpcUrl);
        
        HttpService httpService = new HttpService(rpcUrl);
        Web3j web3j = Web3j.build(httpService);
        
        // Test connection
        try {
            var chainIdResponse = web3j.ethChainId().send();
            if (chainIdResponse.hasError()) {
                log.warn("Failed to get chain ID from zkSync Era: {}", chainIdResponse.getError().getMessage());
            } else {
                log.info("Connected to zkSync Era network with chain ID: {}", chainIdResponse.getChainId());
            }
        } catch (Exception e) {
            log.warn("Could not verify zkSync Era connection: {}", e.getMessage());
        }
        
        return web3j;
    }
    
    /**
     * Get zkSync Era configuration info
     */
    public String getConfigInfo() {
        return String.format(
            "zkSync Era Configuration:\n" +
            "RPC URL: %s\n" +
            "Paymaster Address: %s\n" +
            "Chain ID: 300 (Sepolia testnet)",
            rpcUrl,
            paymasterAddress
        );
    }
}
