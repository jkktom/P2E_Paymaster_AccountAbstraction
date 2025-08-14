package com.blooming.blockchain.springbackend.points.controller;

import com.blooming.blockchain.springbackend.auth.jwt.JwtService;
import com.blooming.blockchain.springbackend.pointtransaction.entity.PointTransaction;
import com.blooming.blockchain.springbackend.pointtransaction.service.PointTransactionService;
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
@RequestMapping("/api/points")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "${app.cors.allowed-origins}", allowCredentials = "true")
public class PointsController {

    private final JwtService jwtService;
    private final UserService userService;
    private final UserPointTokenService userPointTokenService;
    private final PointTransactionService pointTransactionService;

    // Get current user's main point balance
    @GetMapping("/main")
    public ResponseEntity<?> getMainPoints(HttpServletRequest request) {
        try {
            String googleId = extractGoogleIdFromRequest(request);
            if (googleId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Invalid or missing token"
                ));
            }

            UserPointToken balance = userPointTokenService.getUserBalance(googleId)
                .orElse(new UserPointToken(googleId, 0, 0, 0L));

            // Calculate total earned and points to token from transactions
            PointTransactionService.UserPointStatistics stats = pointTransactionService.getUserStatistics(googleId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "balance", balance.getMainPoint(),
                "totalEarned", stats.getTotalMainPointsEarned(),
                "pointsToToken", stats.getTotalMainPointsExchanged()
            ));
        } catch (Exception e) {
            log.error("Failed to get main points for user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to retrieve main points"
            ));
        }
    }

    // Get current user's sub point balance
    @GetMapping("/sub")
    public ResponseEntity<?> getSubPoints(HttpServletRequest request) {
        try {
            String googleId = extractGoogleIdFromRequest(request);
            if (googleId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Invalid or missing token"
                ));
            }

            UserPointToken balance = userPointTokenService.getUserBalance(googleId)
                .orElse(new UserPointToken(googleId, 0, 0, 0L));

            // Calculate total earned and sub to main from transactions
            PointTransactionService.UserPointStatistics stats = pointTransactionService.getUserStatistics(googleId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "balance", balance.getSubPoint(),
                "totalEarned", stats.getTotalSubPointsEarned(),
                "subToMain", stats.getTotalSubPointsConverted()
            ));
        } catch (Exception e) {
            log.error("Failed to get sub points for user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to retrieve sub points"
            ));
        }
    }

    // Convert sub points to main points
    @PostMapping("/convert-sub-to-main")
    public ResponseEntity<?> convertSubToMain(@RequestBody Map<String, Integer> request, HttpServletRequest httpRequest) {
        try {
            String googleId = extractGoogleIdFromRequest(httpRequest);
            if (googleId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Invalid or missing token"
                ));
            }

            Integer subPoints = request.get("subPoints");
            if (subPoints == null || subPoints <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid sub points amount"
                ));
            }

            // Execute conversion through transaction service
            PointTransaction transaction = pointTransactionService.convertSubToMainPoints(googleId, subPoints);
            
            if (transaction == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Insufficient sub points or invalid conversion amount"
                ));
            }

            // Calculate main points received using current ratio
            Integer mainPointsReceived = subPoints / PointTransaction.getSubToMainRatio();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "mainPointsReceived", mainPointsReceived,
                "conversionRate", PointTransaction.getSubToMainRatio(),
                "message", "Points converted successfully"
            ));
        } catch (Exception e) {
            log.error("Failed to convert sub to main points", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to convert points"
            ));
        }
    }

    // Get current user's complete balance info
    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(HttpServletRequest request) {
        try {
            String googleId = extractGoogleIdFromRequest(request);
            if (googleId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Invalid or missing token"
                ));
            }

            UserPointToken balance = userPointTokenService.getUserBalance(googleId)
                .orElse(new UserPointToken(googleId, 0, 0, 0L));

            return ResponseEntity.ok(Map.of(
                "success", true,
                "mainPoint", balance.getMainPoint(),
                "subPoint", balance.getSubPoint(),
                "tokenBalance", balance.getTokenBalance(),
                "updatedAt", balance.getUpdatedAt()
            ));
        } catch (Exception e) {
            log.error("Failed to get balance for user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to retrieve balance"
            ));
        }
    }

    // Helper method to extract Google ID from JWT token
    private String extractGoogleIdFromRequest(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return null;
            }

            String token = authHeader.substring(7);
            jwtService.validateToken(token);
            return jwtService.extractGoogleId(token);
        } catch (Exception e) {
            log.error("Failed to extract Google ID from token", e);
            return null;
        }
    }
}