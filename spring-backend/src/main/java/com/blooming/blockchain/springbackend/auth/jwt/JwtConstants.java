package com.blooming.blockchain.springbackend.auth.jwt;

/**
 * JWT Security and Time Constants
 * 
 * This class centralizes JWT-related magic numbers to improve code maintainability
 * and make security requirements explicit.
 */
public final class JwtConstants {
    
    // Prevent instantiation
    private JwtConstants() {}
    
    // =============== Cryptographic Constants ===============
    
    /**
     * Minimum key length in bytes for HS512 algorithm
     * HS512 uses HMAC with SHA-512, requiring 512 bits (64 bytes) minimum for security
     * 
     */
    public static final int HS512_MIN_KEY_BYTES = 64;
    
    /**
     * HS512 key size in bits (for documentation/validation)
     */
    public static final int HS512_KEY_BITS = HS512_MIN_KEY_BYTES * 8; // 512 bits
    
    // =============== Time Conversion Constants ===============
    
    public static final int SECONDS_TO_MILLIS = 1000;
    
    public static final int MINUTES_TO_SECONDS = 60;
    
    // =============== Token Refresh Configuration ===============
    
    public static final int TOKEN_REFRESH_THRESHOLD_MINUTES = 10;
    
    public static final long TOKEN_REFRESH_THRESHOLD_MILLIS = 
        TOKEN_REFRESH_THRESHOLD_MINUTES * MINUTES_TO_SECONDS * SECONDS_TO_MILLIS;
    
    // =============== Security Validation Methods ===============
    
    /**
     * Validate if key length meets HS512 security requirements
     * @param keyLengthBytes the key length in bytes
     * @return true if key is secure for HS512, false otherwise
     */
    public static boolean isValidHS512KeyLength(int keyLengthBytes) {
        return keyLengthBytes >= HS512_MIN_KEY_BYTES;
    }
    
    /**
     * Get human-readable description of HS512 key requirements
     * @return security requirement description
     */
    public static String getHS512KeyRequirement() {
        return String.format("HS512 requires at least %d bytes (%d bits) for cryptographic security", 
                           HS512_MIN_KEY_BYTES, HS512_KEY_BITS);
    }
}