package com.blooming.blockchain.springbackend.exception;

/**
 * Custom exception for blockchain transaction failures
 * Used when blockchain interactions fail during token exchanges
 */
public class BlockchainTransactionException extends RuntimeException {
    
    public BlockchainTransactionException(String message) {
        super(message);
    }
    
    public BlockchainTransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}