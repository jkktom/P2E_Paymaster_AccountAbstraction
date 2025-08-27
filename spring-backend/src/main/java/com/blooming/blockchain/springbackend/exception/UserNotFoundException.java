package com.blooming.blockchain.springbackend.exception;

/**
 * Custom exception for user not found errors
 * Used when requested user does not exist in the system
 */
public class UserNotFoundException extends RuntimeException {
    
    public UserNotFoundException(String message) {
        super(message);
    }
    
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}