package com.blooming.blockchain.springbackend.points.controller;

import com.blooming.blockchain.springbackend.auth.jwt.JwtService;
import com.blooming.blockchain.springbackend.exception.AuthenticationException;
import com.blooming.blockchain.springbackend.exception.InsufficientPointsException;
import com.blooming.blockchain.springbackend.exception.InvalidRequestException;
import com.blooming.blockchain.springbackend.pointtransaction.entity.PointTransaction;
import com.blooming.blockchain.springbackend.pointtransaction.service.PointTransactionService;
import com.blooming.blockchain.springbackend.points.dto.PointsResponse;
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
        String googleId = extractGoogleIdFromRequest(request);

        UserPointToken balance = userPointTokenService.getUserBalance(googleId)
            .orElse(new UserPointToken(googleId, 0, 0, 0L));

        // Calculate total earned and points to token from transactions
        PointTransactionService.UserPointStatistics stats = pointTransactionService.getUserStatistics(googleId);

        return ResponseEntity.ok(PointsResponse.builder()
            .success(true)
            .balance(balance.getMainPoint())
            .totalEarned(stats.getTotalMainPointsEarned())
            .pointsToToken(stats.getTotalMainPointsExchanged())
            .build());
    }

    // Get current user's sub point balance
    @GetMapping("/sub")
    public ResponseEntity<?> getSubPoints(HttpServletRequest request) {
        String googleId = extractGoogleIdFromRequest(request);

        UserPointToken balance = userPointTokenService.getUserBalance(googleId)
            .orElse(new UserPointToken(googleId, 0, 0, 0L));

        // Calculate total earned and sub to main from transactions
        PointTransactionService.UserPointStatistics stats = pointTransactionService.getUserStatistics(googleId);

        return ResponseEntity.ok(PointsResponse.builder()
            .success(true)
            .balance(balance.getSubPoint())
            .totalEarned(stats.getTotalSubPointsEarned())
            .subToMain(stats.getTotalSubPointsConverted())
            .build());
    }

    // Convert sub points to main points
    @PostMapping("/convert-sub-to-main")
    public ResponseEntity<?> convertSubToMain(@RequestBody Map<String, Integer> request, HttpServletRequest httpRequest) {
        String googleId = extractGoogleIdFromRequest(httpRequest);

        Integer subPoints = request.get("subPoints");
        if (subPoints == null || subPoints <= 0) {
            throw new InvalidRequestException("Invalid sub points amount");
        }

        // Execute conversion through transaction service
        PointTransaction transaction = pointTransactionService.convertSubToMainPoints(googleId, subPoints);
        
        if (transaction == null) {
            throw new InsufficientPointsException("Insufficient sub points or invalid conversion amount");
        }

        // Calculate main points received using current ratio
        Integer mainPointsReceived = subPoints / PointTransaction.getSubToMainRatio();
        
        return ResponseEntity.ok(PointsResponse.builder()
            .success(true)
            .mainPointsReceived(mainPointsReceived)
            .conversionRate((int) PointTransaction.getSubToMainRatio())
            .message("Points converted successfully")
            .build());
    }

    // Get current user's complete balance info
    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(HttpServletRequest request) {
        String googleId = extractGoogleIdFromRequest(request);

        UserPointToken balance = userPointTokenService.getUserBalance(googleId)
            .orElse(new UserPointToken(googleId, 0, 0, 0L));

        return ResponseEntity.ok(PointsResponse.builder()
            .success(true)
            .balance(balance.getMainPoint())
            .subToMain(balance.getSubPoint())
            .tokenBalance(balance.getTokenBalance())
            .updatedAt(balance.getUpdatedAt().toString())
            .build());
    }

    // Helper method to extract Google ID from JWT token
    private String extractGoogleIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AuthenticationException("Invalid or missing token");
        }

        String token = authHeader.substring(7);
        try {
            jwtService.validateToken(token);
            return jwtService.extractGoogleId(token);
        } catch (Exception e) {
            throw new AuthenticationException("Failed to validate token", e);
        }
    }
}