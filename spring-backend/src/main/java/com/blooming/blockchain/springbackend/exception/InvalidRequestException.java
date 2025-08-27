package com.blooming.blockchain.springbackend.exception;

/**
 * Custom exception for invalid request parameters
 * Used when request data is malformed, missing required fields, or contains invalid values
 */
public class InvalidRequestException extends RuntimeException {
    
    public InvalidRequestException(String message) {
        super(message);
    }
    
    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}