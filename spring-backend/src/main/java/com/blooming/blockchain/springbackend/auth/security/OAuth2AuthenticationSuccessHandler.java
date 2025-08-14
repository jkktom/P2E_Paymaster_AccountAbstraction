package com.blooming.blockchain.springbackend.auth.security;

import com.blooming.blockchain.springbackend.auth.dto.AuthResponse;
import com.blooming.blockchain.springbackend.auth.dto.ErrorResponse;
import com.blooming.blockchain.springbackend.auth.dto.UserResponse;
import com.blooming.blockchain.springbackend.auth.jwt.JwtService;
import com.blooming.blockchain.springbackend.user.entity.User;
import com.blooming.blockchain.springbackend.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException {
        if (!(authentication instanceof OAuth2AuthenticationToken oauth2Token)) {
            log.error("Invalid authentication type: {}", authentication.getClass().getSimpleName());
            sendErrorResponse(response, "Invalid authentication");
            return;
        }

        try {
            OAuth2User oauth2User = oauth2Token.getPrincipal();
            String googleId = oauth2User.getAttribute("sub");
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            String avatar = oauth2User.getAttribute("picture");

            log.info("OAuth2 authentication success for user: {} ({})", name, email);

            User user = userService.createOrUpdateUser(googleId, email, name, avatar);
            String jwtToken = jwtService.generateToken(googleId, email, name, user.getRoleId(), user.getSmartWalletAddress());

            UserResponse userResponse = UserResponse.builder()
                .googleId(user.getGoogleId())
                .email(user.getEmail())
                .name(user.getName())
                .avatar(user.getAvatar())
                .smartWalletAddress(user.getSmartWalletAddress())
                .roleId(user.getRoleId())
                .createdAt(user.getCreatedAt())
                .build();

            AuthResponse authResponse = AuthResponse.builder()
                .success(true)
                .message("Authentication successful")
                .token(jwtToken)
                .user(userResponse)
                .expiresIn(jwtService.getExpirationTime())
                .build();

            // Redirect to frontend with token
            String redirectUrl = frontendUrl + "/auth/callback?token=" + jwtToken + 
                               "&user=" + java.net.URLEncoder.encode(user.getName(), "UTF-8");
            
            log.info("Redirecting to frontend with token for user: {}", email);
            log.info("Full redirect URL: {}", redirectUrl);
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            log.error("OAuth2 authentication success handling failed", e);
            sendErrorResponse(response, "Authentication processing failed");
        }
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        ErrorResponse errorResponse = ErrorResponse.builder()
            .success(false)
            .message(message)
            .error("OAUTH2_ERROR")
            .build();

        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", frontendUrl);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
