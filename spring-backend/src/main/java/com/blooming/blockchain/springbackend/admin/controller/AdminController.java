package com.blooming.blockchain.springbackend.admin.controller;

import com.blooming.blockchain.springbackend.auth.jwt.JwtService;
import com.blooming.blockchain.springbackend.global.enums.PointTransactionSource;
import com.blooming.blockchain.springbackend.global.enums.TransactionStatusType;
import com.blooming.blockchain.springbackend.pointtransaction.entity.PointTransaction;
import com.blooming.blockchain.springbackend.pointtransaction.service.PointTransactionService;
import com.blooming.blockchain.springbackend.user.entity.User;
import com.blooming.blockchain.springbackend.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "${app.cors.allowed-origins}", allowCredentials = "true")
public class AdminController {

    private final JwtService jwtService;
    private final UserService userService;
    private final PointTransactionService pointTransactionService;

    // Grant points to user (admin only)
    @PostMapping("/grant-points")
    public ResponseEntity<?> grantPoints(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            // Extract and validate admin user
            String adminGoogleId = extractGoogleIdFromRequest(httpRequest);
            if (adminGoogleId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Invalid or missing token"
                ));
            }

            // For development/demo purposes, allow any authenticated user to grant points
            // In production, you would uncomment the role check below:
            // Byte roleId = extractRoleIdFromRequest(httpRequest);
            // if (roleId == null || roleId != 1) {
            //     return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
            //         "success", false,
            //         "message", "Admin role required"
            //     ));
            // }

            // Extract request parameters
            String userGoogleId = (String) request.get("userGoogleId");
            String pointType = (String) request.get("pointType");
            Integer amount = null;
            Object amountObj = request.get("amount");
            if (amountObj instanceof Integer) {
                amount = (Integer) amountObj;
            } else if (amountObj instanceof Number) {
                amount = ((Number) amountObj).intValue();
            }
            String description = (String) request.get("description");

            // Validate parameters
            if (pointType == null || amount == null || amount <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid parameters"
                ));
            }

            // If userGoogleId is not provided, grant to the current user (for demo purposes)
            String targetGoogleId = userGoogleId != null ? userGoogleId : adminGoogleId;

            PointTransaction transaction = null;
            
            // Execute point granting based on type using proper enums instead of magic numbers
            if ("MAIN".equals(pointType)) {
                transaction = pointTransactionService.earnMainPoints(
                    targetGoogleId, 
                    amount, 
                    PointTransactionSource.MAIN_ADMIN_GRANT.getId(), 
                    description
                );
            } else if ("SUB".equals(pointType)) {
                transaction = pointTransactionService.earnSubPoints(
                    targetGoogleId, 
                    amount, 
                    PointTransactionSource.SUB_ADMIN_GRANT.getId(), 
                    description
                );
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid point type. Must be 'MAIN' or 'SUB'"
                ));
            }

            if (transaction != null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "transaction", Map.of(
                        "id", transaction.getId(),
                        "amount", amount,
                        "pointType", pointType,
                        "status", TransactionStatusType.fromId(transaction.getTransactionStatusId()).getName()
                    ),
                    "message", "Points granted successfully"
                ));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to grant points"
                ));
            }

        } catch (Exception e) {
            log.error("Failed to grant points", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to grant points: " + e.getMessage()
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

    // Helper method to extract Role ID from JWT token
    private Byte extractRoleIdFromRequest(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return null;
            }

            String token = authHeader.substring(7);
            jwtService.validateToken(token);
            return jwtService.extractRoleId(token);
        } catch (Exception e) {
            log.error("Failed to extract Role ID from token", e);
            return null;
        }
    }
}