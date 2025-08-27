package com.blooming.blockchain.springbackend.auth.controller;

import com.blooming.blockchain.springbackend.auth.dto.*;
import com.blooming.blockchain.springbackend.auth.jwt.JwtService;
import com.blooming.blockchain.springbackend.global.enums.RoleType;
import com.blooming.blockchain.springbackend.user.entity.User;
import com.blooming.blockchain.springbackend.user.service.UserService;
import com.blooming.blockchain.springbackend.userdetail.entity.UserPointToken;
import com.blooming.blockchain.springbackend.userdetail.service.UserPointTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "${app.cors.allowed-origins}", allowCredentials = "true")
public class AuthController {

    private final JwtService jwtService;
    private final UserService userService;
    private final UserPointTokenService userPointTokenService;

    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.builder()
                .success(false)
                .message("No authorization token provided")
                .build());
        }

        String token = authHeader.substring(7);
        jwtService.validateToken(token); // Throws JwtException if invalid

        String googleId = jwtService.extractGoogleId(token);
        Optional<User> userOpt = userService.findByGoogleId(googleId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.builder()
                .success(false)
                .message("User not found")
                .build());
        }

        User user = userOpt.get();
        UserPointToken balance = userPointTokenService.getUserBalance(googleId)
            .orElse(new UserPointToken(googleId, 0, 0, 0L));

        UserResponse userResponse = UserResponse.builder()
            .googleId(user.getGoogleId())
            .email(user.getEmail())
            .name(user.getName())
            .avatar(user.getAvatar())
            .smartWalletAddress(user.getSmartWalletAddress())
            .roleId(user.getRoleId())
            .createdAt(user.getCreatedAt())
            .build();

        BalanceResponse balanceResponse = BalanceResponse.builder()
            .mainPoint(balance.getMainPoint())
            .subPoint(balance.getSubPoint())
            .tokenBalance(balance.getTokenBalance())
            .updatedAt(balance.getUpdatedAt())
            .build();

        return ResponseEntity.ok(CurrentUserResponse.builder()
            .success(true)
            .user(userResponse)
            .balance(balanceResponse)
            .build());
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidateResponse> validateToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ValidateResponse.builder()
                .success(false)
                .message("Token is required")
                .build());
        }

        boolean isValid = jwtService.validateToken(token);
        if (!isValid) {
            return ResponseEntity.ok(ValidateResponse.builder().success(true).valid(false).build());
        }

        String googleId = jwtService.extractGoogleId(token);
        UserResponse userResponse = UserResponse.builder()
            .googleId(googleId)
            .email(jwtService.extractEmail(token))
            .name(jwtService.extractName(token))
            .smartWalletAddress(jwtService.extractSmartWalletAddress(token))
            .roleId(jwtService.extractRoleId(token))
            .build();

        return ResponseEntity.ok(ValidateResponse.builder()
            .success(true)
            .valid(true)
            .user(userResponse)
            .shouldRefresh(jwtService.shouldRefreshToken(token))
            .build());
    }

    /**
     * Refresh JWT Token Endpoint
     * 
     * This endpoint can refresh both valid and expired tokens as long as they are:
     * - Properly signed with the correct secret
     * - Not tampered with (signature is valid)
     * - Contains valid user claims
     * 
     * Security Note: Expiration is ignored during refresh to allow users to refresh
     * expired tokens, but signature validation is still enforced.
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.builder()
                .success(false)
                .message("No authorization token provided")
                .build());
        }

        String oldToken = authHeader.substring(7);
        
        // Use refresh-specific extraction methods that ignore expiration
        // This allows refreshing expired tokens as long as they're properly signed
        String googleId;
        String email;
        String name;
        Byte roleId;
        String smartWalletAddress;
        
        try {
            googleId = jwtService.extractGoogleIdForRefresh(oldToken);
            email = jwtService.extractEmailForRefresh(oldToken);
            name = jwtService.extractNameForRefresh(oldToken);
            roleId = jwtService.extractRoleIdForRefresh(oldToken);
            smartWalletAddress = jwtService.extractSmartWalletAddressForRefresh(oldToken);
        } catch (Exception e) {
            log.error("Failed to extract claims from token for refresh: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.builder()
                .success(false)
                .message("Invalid token signature or format")
                .build());
        }
        
        if (googleId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.builder()
                .success(false)
                .message("Invalid token: missing Google ID")
                .build());
        }
        
        // Fetch from database for backward compatibility or missing fields
        Optional<User> userOpt = userService.findByGoogleId(googleId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (roleId == null) {
                roleId = user.getRoleId();
            }
            if (smartWalletAddress == null) {
                smartWalletAddress = user.getSmartWalletAddress();
            }
        } else {
            roleId = RoleType.USER.getId(); // Default to USER role
        }
        
        String newToken = jwtService.generateToken(googleId, email, name, roleId, smartWalletAddress);

        // Check if the old token was expired for informational purposes
        boolean wasExpired = false;
        try {
            wasExpired = jwtService.isTokenExpired(oldToken);
        } catch (Exception e) {
            // Token was invalid/expired, but we already extracted claims successfully above
            wasExpired = true;
        }
        
        String message = wasExpired ? 
            "Expired token refreshed successfully" : 
            "Token refreshed successfully";

        return ResponseEntity.ok(AuthResponse.builder()
            .success(true)
            .token(newToken)
            .expiresIn(jwtService.getExpirationTime())
            .message(message)
            .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout() {
        return ResponseEntity.ok(LogoutResponse.builder()
            .success(true)
            .message("Logout successful")
            .build());
    }

    @GetMapping("/login/google")
    public ResponseEntity<LoginUrlResponse> getGoogleLoginUrl(HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() +
            (request.getServerPort() != 80 && request.getServerPort() != 443 ? ":" + request.getServerPort() : "");
        String googleLoginUrl = baseUrl + "/oauth2/authorization/google";

        return ResponseEntity.ok(LoginUrlResponse.builder()
            .success(true)
            .loginUrl(googleLoginUrl)
            .message("Google OAuth2 login URL")
            .build());
    }

    @PostMapping("/google")
    public ResponseEntity<LoginUrlResponse> handleGoogleAuth(HttpServletRequest request) {
        log.info("Google auth endpoint called from: {}", request.getRemoteAddr());
        
        String baseUrl = request.getScheme() + "://" + request.getServerName() +
            (request.getServerPort() != 80 && request.getServerPort() != 443 ? ":" + request.getServerPort() : "");
        String googleLoginUrl = baseUrl + "/oauth2/authorization/google";
        
        log.info("Generated Google login URL: {}", googleLoginUrl);

        LoginUrlResponse response = LoginUrlResponse.builder()
            .success(true)
            .loginUrl(googleLoginUrl)
            .message("Redirecting to Google OAuth2")
            .build();
            
        log.info("Sending response: {}", response);
        return ResponseEntity.ok(response);
    }
}
