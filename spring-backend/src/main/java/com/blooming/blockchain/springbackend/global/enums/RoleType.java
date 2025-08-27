package com.blooming.blockchain.springbackend.global.enums;

/**
 * Enum for User Role Types
 * Maps to Role entity reference data
 * 
 * This enum provides type-safe constants to replace magic numbers in role handling.
 */
public enum RoleType {
    
    ADMIN((byte) 1, "ADMIN"),
    USER((byte) 2, "USER");

    private final byte id;
    private final String name;

    RoleType(byte id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Get the database ID for this role
     * @return byte ID used in database
     */
    public byte getId() {
        return id;
    }

    /**
     * Get the name of this role
     * @return string name
     */
    public String getName() {
        return name;
    }

    /**
     * Check if this is an admin role
     * @return true if admin, false otherwise
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }

    /**
     * Check if this is a user role
     * @return true if user, false otherwise
     */
    public boolean isUser() {
        return this == USER;
    }

    /**
     * Get enum by database ID
     * @param id database ID
     * @return corresponding enum value
     * @throws IllegalArgumentException if ID not found
     */
    public static RoleType fromId(byte id) {
        for (RoleType role : values()) {
            if (role.id == id) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role ID: " + id);
    }

    /**
     * Get enum by name
     * @param name role name
     * @return corresponding enum value
     * @throws IllegalArgumentException if name not found
     */
    public static RoleType fromName(String name) {
        for (RoleType role : values()) {
            if (role.name.equals(name)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role name: " + name);
    }
}