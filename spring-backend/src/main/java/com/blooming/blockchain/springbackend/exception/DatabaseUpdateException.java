package com.blooming.blockchain.springbackend.exception;

/**
 * Custom exception for database update failures
 * Used when database operations fail during transactions
 */
public class DatabaseUpdateException extends RuntimeException {
    
    public DatabaseUpdateException(String message) {
        super(message);
    }
    
    public DatabaseUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}