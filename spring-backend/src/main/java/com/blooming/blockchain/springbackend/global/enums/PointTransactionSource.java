package com.blooming.blockchain.springbackend.global.enums;

/**
 * Enum for Point Transaction Sources
 * Maps to PointEarnSpendSource entity reference data
 * 
 * This enum provides type-safe constants to replace magic numbers in transaction handling,
 * making the code more maintainable and self-documenting.
 */
public enum PointTransactionSource {
    
    // MAIN point transactions (1-6)
    MAIN_TASK_COMPLETION((byte) 1, "MAIN_TASK_COMPLETION", "EARNING"),
    MAIN_EVENT_REWARD((byte) 2, "MAIN_EVENT_REWARD", "EARNING"),
    MAIN_ADMIN_GRANT((byte) 3, "MAIN_ADMIN_GRANT", "EARNING"),
    MAIN_OTHERS_EARN((byte) 4, "MAIN_OTHERS_EARN", "EARNING"),
    MAIN_EXCHANGE((byte) 5, "MAIN_EXCHANGE", "SPENDING"), // main points → governance tokens
    MAIN_SPEND_OTHERS((byte) 6, "MAIN_SPEND_OTHERS", "SPENDING"),
    
    // SUB point transactions (7-12)
    SUB_TASK_COMPLETION((byte) 7, "SUB_TASK_COMPLETION", "EARNING"),
    SUB_EVENT_REWARD((byte) 8, "SUB_EVENT_REWARD", "EARNING"),
    SUB_ADMIN_GRANT((byte) 9, "SUB_ADMIN_GRANT", "EARNING"),
    SUB_OTHERS_EARN((byte) 10, "SUB_OTHERS_EARN", "EARNING"),
    SUB_CONVERSION((byte) 11, "SUB_CONVERSION", "SPENDING"), // sub points → main points
    SUB_SPEND_OTHERS((byte) 12, "SUB_SPEND_OTHERS", "SPENDING");

    private final byte id;
    private final String name;
    private final String type;

    PointTransactionSource(byte id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    /**
     * Get the database ID for this transaction source
     * @return byte ID used in database
     */
    public byte getId() {
        return id;
    }

    /**
     * Get the name of this transaction source
     * @return string name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the transaction type (EARNING or SPENDING)
     * @return transaction type
     */
    public String getType() {
        return type;
    }

    /**
     * Check if this is an earning source
     * @return true if earning, false if spending
     */
    public boolean isEarning() {
        return "EARNING".equals(type);
    }

    /**
     * Check if this is a spending source
     * @return true if spending, false if earning
     */
    public boolean isSpending() {
        return "SPENDING".equals(type);
    }

    /**
     * Get enum by database ID
     * @param id database ID
     * @return corresponding enum value
     * @throws IllegalArgumentException if ID not found
     */
    public static PointTransactionSource fromId(byte id) {
        for (PointTransactionSource source : values()) {
            if (source.id == id) {
                return source;
            }
        }
        throw new IllegalArgumentException("Unknown transaction source ID: " + id);
    }

    /**
     * Get enum by name
     * @param name source name
     * @return corresponding enum value
     * @throws IllegalArgumentException if name not found
     */
    public static PointTransactionSource fromName(String name) {
        for (PointTransactionSource source : values()) {
            if (source.name.equals(name)) {
                return source;
            }
        }
        throw new IllegalArgumentException("Unknown transaction source name: " + name);
    }
}