package com.blooming.blockchain.springbackend.exception;

import lombok.Getter;

/**
 * Custom exception for insufficient points errors
 * Used when user tries to spend more points than available
 */
@Getter
public class InsufficientPointsException extends RuntimeException {
    
    private final Integer currentPoints;
    private final Integer requiredPoints;
    
    public InsufficientPointsException(String message) {
        super(message);
        this.currentPoints = null;
        this.requiredPoints = null;
    }
    
    public InsufficientPointsException(int currentPoints, int requiredPoints) {
        super(String.format("Insufficient main points. You have %d, need %d for exchange", currentPoints, requiredPoints));
        this.currentPoints = currentPoints;
        this.requiredPoints = requiredPoints;
    }
    
    public InsufficientPointsException(String message, Throwable cause) {
        super(message, cause);
        this.currentPoints = null;
        this.requiredPoints = null;
    }
}