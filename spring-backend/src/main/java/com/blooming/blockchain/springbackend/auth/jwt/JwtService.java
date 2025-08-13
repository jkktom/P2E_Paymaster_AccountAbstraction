package com.blooming.blockchain.springbackend.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    // Generate JWT token for user
    public String generateToken(String googleId, String email, String name) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("name", name);
        claims.put("googleId", googleId);
        
        return createToken(claims, googleId);
    }

    // Create JWT token with claims
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration * 1000);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // Extract Google ID from token
    public String extractGoogleId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract email from token
    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    // Extract name from token
    public String extractName(String token) {
        return extractClaim(token, claims -> claims.get("name", String.class));
    }

    // Extract expiration date from token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extract specific claim from token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extract all claims from token
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("Failed to parse JWT token: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    // Check if token is expired
    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            log.error("Failed to check token expiration: {}", e.getMessage());
            return true;
        }
    }

    // Validate JWT token
    public boolean validateToken(String token, String googleId) {
        try {
            final String tokenGoogleId = extractGoogleId(token);
            return (tokenGoogleId.equals(googleId) && !isTokenExpired(token));
        } catch (Exception e) {
            log.error("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

    // Validate JWT token without user check
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

    // Get signing key from secret
    private Key getSigningKey() {
        try {
            // Try to use the configured secret if it's valid
            byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
            if (keyBytes.length >= 64) { // HS512 requires at least 64 bytes (512 bits)
                return Keys.hmacShaKeyFor(keyBytes);
            }
        } catch (Exception e) {
            log.warn("Invalid or weak JWT secret, generating secure key: {}", e.getMessage());
        }
        
        // Generate a secure key for HS512 if the configured secret is too weak
        return Keys.secretKeyFor(SignatureAlgorithm.HS512);
    }

    // Get JWT expiration time in milliseconds
    public long getExpirationTime() {
        return jwtExpiration * 1000;
    }

    // Check if token needs refresh (expires in next 10 minutes)
    public boolean shouldRefreshToken(String token) {
        try {
            Date expiration = extractExpiration(token);
            Date tenMinutesFromNow = new Date(System.currentTimeMillis() + 10 * 60 * 1000);
            return expiration.before(tenMinutesFromNow);
        } catch (Exception e) {
            return true; // If we can't determine, assume refresh is needed
        }
    }
}