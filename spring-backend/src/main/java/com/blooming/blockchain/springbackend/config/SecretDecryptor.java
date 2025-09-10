package com.blooming.blockchain.springbackend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Decrypts XOR-encrypted secrets BEFORE Spring processes OAuth2 configuration
 * Uses BeanFactoryPostProcessor to run early in Spring lifecycle
 */
@Component
@Slf4j
public class SecretDecryptor implements BeanFactoryPostProcessor {

    private static final String SERVICE_KEY = "bloomServiceKey";

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        ConfigurableEnvironment environment = beanFactory.getBean(ConfigurableEnvironment.class);
        
        log.info("Decrypting application secrets early in Spring lifecycle...");
        
        Map<String, Object> decryptedProperties = new HashMap<>();
        
        // Get encrypted values from environment
        String googleClientSecretXor = environment.getProperty("spring.security.oauth2.client.registration.google.client-secret-xor", "");
        String zkSyncPrivateKeyXor = environment.getProperty("app.zksync.owner.private-key-xor", "");
        
        // Decrypt Google OAuth client secret
        if (!googleClientSecretXor.isEmpty()) {
            try {
                String decryptedGoogleSecret = xorDecrypt(googleClientSecretXor, SERVICE_KEY);
                decryptedProperties.put("spring.security.oauth2.client.registration.google.client-secret", decryptedGoogleSecret);
                log.info("Successfully decrypted Google OAuth client secret");
            } catch (Exception e) {
                log.error("Failed to decrypt Google OAuth client secret", e);
            }
        }
        
        // Decrypt zkSync owner private key
        if (!zkSyncPrivateKeyXor.isEmpty()) {
            try {
                String decryptedZkSyncKey = xorDecrypt(zkSyncPrivateKeyXor, SERVICE_KEY);
                decryptedProperties.put("app.zksync.owner.private-key", decryptedZkSyncKey);
                log.info("Successfully decrypted zkSync owner private key");
            } catch (Exception e) {
                log.error("Failed to decrypt zkSync owner private key", e);
            }
        }
        
        // Add decrypted properties to Spring environment (highest priority)
        if (!decryptedProperties.isEmpty()) {
            MapPropertySource decryptedSource = new MapPropertySource("decryptedSecrets", decryptedProperties);
            environment.getPropertySources().addFirst(decryptedSource);
            log.info("Added {} decrypted secrets to Spring environment", decryptedProperties.size());
        }
    }
    
    /**
     * Simple XOR decryption with Base64 decoding
     */
    private String xorDecrypt(String encryptedData, String key) {
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedData);
            String data = new String(decoded);
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < data.length(); i++) {
                result.append((char) (data.charAt(i) ^ key.charAt(i % key.length())));
            }
            return result.toString();
        } catch (Exception e) {
            throw new RuntimeException("XOR decryption failed", e);
        }
    }
}