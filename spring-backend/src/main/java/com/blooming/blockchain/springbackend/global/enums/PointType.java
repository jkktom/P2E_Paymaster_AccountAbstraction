package com.blooming.blockchain.springbackend.global.enums;

/**
 * Enum for Point Types
 * Used in PointTransaction entity to distinguish between MAIN and SUB points
 * 
 * This enum provides type-safe constants to replace magic numbers in point type handling.
 */
public enum PointType {
    
    MAIN((byte) 1, "MAIN"),
    SUB((byte) 2, "SUB");

    private final byte id;
    private final String name;

    PointType(byte id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Get the database ID for this point type
     * @return byte ID used in database
     */
    public byte getId() {
        return id;
    }

    /**
     * Get the name of this point type
     * @return string name
     */
    public String getName() {
        return name;
    }

    /**
     * Check if this is MAIN point type
     * @return true if MAIN, false otherwise
     */
    public boolean isMain() {
        return this == MAIN;
    }

    /**
     * Check if this is SUB point type
     * @return true if SUB, false otherwise
     */
    public boolean isSub() {
        return this == SUB;
    }

    /**
     * Get enum by database ID
     * @param id database ID
     * @return corresponding enum value
     * @throws IllegalArgumentException if ID not found
     */
    public static PointType fromId(byte id) {
        for (PointType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown point type ID: " + id);
    }

    /**
     * Get enum by name
     * @param name type name
     * @return corresponding enum value
     * @throws IllegalArgumentException if name not found
     */
    public static PointType fromName(String name) {
        for (PointType type : values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown point type name: " + name);
    }
}