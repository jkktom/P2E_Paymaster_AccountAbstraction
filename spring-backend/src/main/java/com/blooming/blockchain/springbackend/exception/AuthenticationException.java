package com.blooming.blockchain.springbackend.exception;

/**
 * Custom exception for authentication-related errors
 * Used when JWT token is invalid, missing, or user authentication fails
 */
public class AuthenticationException extends RuntimeException {
    
    public AuthenticationException(String message) {
        super(message);
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}