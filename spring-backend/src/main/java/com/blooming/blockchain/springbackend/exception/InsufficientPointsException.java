package com.blooming.blockchain.springbackend.exception;

/**
 * Custom exception for insufficient points errors
 * Used when user tries to spend more points than available
 */
public class InsufficientPointsException extends RuntimeException {
    
    public InsufficientPointsException(String message) {
        super(message);
    }
    
    public InsufficientPointsException(String message, Throwable cause) {
        super(message, cause);
    }
}