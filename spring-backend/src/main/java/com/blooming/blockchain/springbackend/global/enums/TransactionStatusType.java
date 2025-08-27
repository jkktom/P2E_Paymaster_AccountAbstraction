package com.blooming.blockchain.springbackend.global.enums;

/**
 * Enum for Transaction Status Types
 * Maps to TransactionStatus entity
 * 
 * This enum provides type-safe constants to replace magic numbers in transaction status handling.
 */
public enum TransactionStatusType {
    
    PENDING((byte) 1, "PENDING"),
    CONFIRMED((byte) 2, "CONFIRMED"),
    FAILED((byte) 3, "FAILED");

    private final byte id;
    private final String name;

    TransactionStatusType(byte id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Get the database ID for this transaction status
     * @return byte ID used in database
     */
    public byte getId() {
        return id;
    }

    /**
     * Get the name of this transaction status
     * @return string name
     */
    public String getName() {
        return name;
    }

    /**
     * Check if this is a confirmed transaction
     * @return true if confirmed, false otherwise
     */
    public boolean isConfirmed() {
        return this == CONFIRMED;
    }

    /**
     * Check if this is a pending transaction
     * @return true if pending, false otherwise
     */
    public boolean isPending() {
        return this == PENDING;
    }

    /**
     * Check if this is a failed transaction
     * @return true if failed, false otherwise
     */
    public boolean isFailed() {
        return this == FAILED;
    }

    /**
     * Get enum by database ID
     * @param id database ID
     * @return corresponding enum value
     * @throws IllegalArgumentException if ID not found
     */
    public static TransactionStatusType fromId(byte id) {
        for (TransactionStatusType status : values()) {
            if (status.id == id) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown transaction status ID: " + id);
    }

    /**
     * Get enum by name
     * @param name status name
     * @return corresponding enum value
     * @throws IllegalArgumentException if name not found
     */
    public static TransactionStatusType fromName(String name) {
        for (TransactionStatusType status : values()) {
            if (status.name.equals(name)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown transaction status name: " + name);
    }
}