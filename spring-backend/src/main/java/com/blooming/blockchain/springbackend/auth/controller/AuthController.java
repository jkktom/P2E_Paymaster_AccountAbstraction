package com.blooming.blockchain.springbackend.auth.controller;

import com.blooming.blockchain.springbackend.auth.jwt.JwtService;
import com.blooming.blockchain.springbackend.user.entity.User;
import com.blooming.blockchain.springbackend.user.service.UserService;
import com.blooming.blockchain.springbackend.userdetail.entity.UserPointToken;
import com.blooming.blockchain.springbackend.userdetail.service.UserPointTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "${app.cors.allowed-origins}", allowCredentials = "true")
public class AuthController {

    private final JwtService jwtService;
    private final UserService userService;
    private final UserPointTokenService userPointTokenService;

    // Get current authenticated user information
    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        try {
            // Extract user info from JWT token
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "No authorization token provided"
                ));
            }

            String token = authHeader.substring(7);
            String googleId = jwtService.extractGoogleId(token);

            if (!jwtService.validateToken(token)) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Invalid or expired token"
                ));
            }

            // Get user from database
            Optional<User> userOpt = userService.findByGoogleId(googleId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            User user = userOpt.get();

            // Get user balance
            Optional<UserPointToken> balance = userPointTokenService.getUserBalance(googleId);
            UserPointToken userBalance = balance.orElse(new UserPointToken(googleId, 0, 0, 0L));

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", Map.of(
                "googleId", user.getGoogleId(),
                "email", user.getEmail(),
                "name", user.getName(),
                "avatar", user.getAvatar(),
                "smartWalletAddress", user.getSmartWalletAddress(),
                "roleId", user.getRoleId(),
                "createdAt", user.getCreatedAt()
            ));
            response.put("balance", Map.of(
                "mainPoint", userBalance.getMainPoint(),
                "subPoint", userBalance.getSubPoint(),
                "tokenBalance", userBalance.getTokenBalance(),
                "updatedAt", userBalance.getUpdatedAt()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting current user", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Internal server error"
            ));
        }
    }

    // Validate JWT token
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Token is required"
                ));
            }

            boolean isValid = jwtService.validateToken(token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("valid", isValid);

            if (isValid) {
                String googleId = jwtService.extractGoogleId(token);
                String email = jwtService.extractEmail(token);
                String name = jwtService.extractName(token);
                
                response.put("user", Map.of(
                    "googleId", googleId,
                    "email", email,
                    "name", name
                ));
                response.put("shouldRefresh", jwtService.shouldRefreshToken(token));
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error validating token", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Token validation failed"
            ));
        }
    }

    // Refresh JWT token
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "No authorization token provided"
                ));
            }

            String oldToken = authHeader.substring(7);
            String googleId = jwtService.extractGoogleId(oldToken);

            // Validate the old token (even if expired, we can refresh if it's structurally valid)
            if (googleId == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Invalid token"
                ));
            }

            // Get user info from token
            String email = jwtService.extractEmail(oldToken);
            String name = jwtService.extractName(oldToken);

            // Generate new token
            String newToken = jwtService.generateToken(googleId, email, name);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("token", newToken);
            response.put("expiresIn", jwtService.getExpirationTime());
            response.put("message", "Token refreshed successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error refreshing token", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Token refresh failed"
            ));
        }
    }

    // Logout (client-side token removal, server doesn't maintain token state)
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Logout successful");
        
        return ResponseEntity.ok(response);
    }

    // Get login URL for frontend
    @GetMapping("/login/google")
    public ResponseEntity<Map<String, Object>> getGoogleLoginUrl(HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + 
                        (request.getServerPort() != 80 && request.getServerPort() != 443 ? ":" + request.getServerPort() : "");
        
        String googleLoginUrl = baseUrl + "/oauth2/authorization/google";
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("loginUrl", googleLoginUrl);
        response.put("message", "Google OAuth2 login URL");
        
        return ResponseEntity.ok(response);
    }
}