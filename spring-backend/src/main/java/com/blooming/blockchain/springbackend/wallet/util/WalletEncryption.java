package com.blooming.blockchain.springbackend.wallet.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.SecretKeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Secure wallet encryption utility using AES-256-GCM
 * Encrypts private keys with user-specific salts derived from email
 */
@Component
@Slf4j
public class WalletEncryption {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 16; // 128 bits
    private static final int KEY_LENGTH = 256; // AES-256
    private static final int SALT_LENGTH = 32; // 256 bits
    private static final int PBKDF2_ITERATIONS = 100000; // Strong iteration count

    @Value("${app.wallet.encryption.admin-key:defaultAdminKeyForDevOnly}")
    private String adminKey;

    /**
     * Encrypt a private key using user-specific encryption
     * @param privateKey the private key to encrypt (without 0x prefix)
     * @param userEmail user email for salt derivation
     * @return Base64 encoded encrypted private key
     * @throws RuntimeException if encryption fails
     */
    public String encrypt(String privateKey, String userEmail) {
        try {
            // Generate random salt for this encryption
            byte[] salt = generateSalt();
            
            // Derive encryption key from master key + user email + salt
            SecretKey secretKey = deriveKey(userEmail, salt);
            
            // Generate random IV for GCM
            byte[] iv = generateIV();
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);
            
            // Encrypt the private key
            byte[] encryptedData = cipher.doFinal(privateKey.getBytes());
            
            // Combine salt + IV + encrypted data
            byte[] combined = new byte[salt.length + iv.length + encryptedData.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(iv, 0, combined, salt.length, iv.length);
            System.arraycopy(encryptedData, 0, combined, salt.length + iv.length, encryptedData.length);
            
            // Return Base64 encoded result
            String result = Base64.getEncoder().encodeToString(combined);
            log.debug("Successfully encrypted private key for user: {}", userEmail);
            
            return result;
            
        } catch (Exception e) {
            log.error("Failed to encrypt private key for user: {}", userEmail, e);
            throw new RuntimeException("Private key encryption failed", e);
        }
    }

    /**
     * Decrypt a private key using user-specific decryption  
     * @param encryptedPrivateKey Base64 encoded encrypted private key
     * @param userEmail user email for key derivation
     * @return decrypted private key (without 0x prefix)
     * @throws RuntimeException if decryption fails
     */
    public String decrypt(String encryptedPrivateKey, String userEmail) {
        try {
            // Decode Base64
            byte[] combined = Base64.getDecoder().decode(encryptedPrivateKey);
            
            // Extract salt, IV, and encrypted data
            byte[] salt = new byte[SALT_LENGTH];
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encryptedData = new byte[combined.length - SALT_LENGTH - GCM_IV_LENGTH];
            
            System.arraycopy(combined, 0, salt, 0, SALT_LENGTH);
            System.arraycopy(combined, SALT_LENGTH, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(combined, SALT_LENGTH + GCM_IV_LENGTH, encryptedData, 0, encryptedData.length);
            
            // Derive the same encryption key
            SecretKey secretKey = deriveKey(userEmail, salt);
            
            // Initialize cipher for decryption
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);
            
            // Decrypt the data
            byte[] decryptedData = cipher.doFinal(encryptedData);
            
            String result = new String(decryptedData);
            log.debug("Successfully decrypted private key for user: {}", userEmail);
            
            return result;
            
        } catch (Exception e) {
            log.error("Failed to decrypt private key for user: {}", userEmail, e);
            throw new RuntimeException("Private key decryption failed", e);
        }
    }

    /**
     * Derive encryption key from master key, user email, and salt using PBKDF2
     */
    private SecretKey deriveKey(String userEmail, byte[] salt) throws Exception {
        // Use admin key + user email as password for key derivation
        String keyMaterial = adminKey + ":" + userEmail.toLowerCase();
        
        KeySpec spec = new PBEKeySpec(keyMaterial.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] derivedKey = factory.generateSecret(spec).getEncoded();
        
        return new SecretKeySpec(derivedKey, ALGORITHM);
    }

    /**
     * Generate cryptographically secure random salt
     */
    private byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    /**
     * Generate cryptographically secure random IV for GCM
     */
    private byte[] generateIV() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    /**
     * Simple encryption for POC using XOR + Base64
     * @param privateKey the private key to encrypt
     * @return Base64 encoded encrypted private key
     */
    public String encryptPrivateKey(String privateKey) {
        return encryptXOR(privateKey, adminKey);
    }

    /**
     * Simple decryption for POC using XOR + Base64
     * @param encryptedPrivateKey Base64 encoded encrypted private key
     * @return decrypted private key
     */
    public String decryptPrivateKey(String encryptedPrivateKey) {
        return decryptXOR(encryptedPrivateKey, adminKey);
    }

    /**
     * XOR encryption with Base64 encoding
     */
    private String encryptXOR(String data, String key) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < data.length(); i++) {
            result.append((char) (data.charAt(i) ^ key.charAt(i % key.length())));
        }
        return Base64.getEncoder().encodeToString(result.toString().getBytes());
    }

    /**
     * XOR decryption with Base64 decoding
     */
    private String decryptXOR(String encryptedData, String key) {
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedData);
            String data = new String(decoded);
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < data.length(); i++) {
                result.append((char) (data.charAt(i) ^ key.charAt(i % key.length())));
            }
            return result.toString();
        } catch (Exception e) {
            log.error("Failed to decrypt with XOR", e);
            throw new RuntimeException("XOR decryption failed", e);
        }
    }

    /**
     * Generate a secure random AES key for testing/development
     * @return Base64 encoded 256-bit key
     */
    public static String generateMasterKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(KEY_LENGTH);
            SecretKey secretKey = keyGen.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate master key", e);
        }
    }
}