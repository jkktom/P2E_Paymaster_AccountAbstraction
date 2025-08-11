package com.blooming.blockchain.springbackend.auth.security;

import com.blooming.blockchain.springbackend.auth.jwt.JwtService;
import com.blooming.blockchain.springbackend.user.entity.User;
import com.blooming.blockchain.springbackend.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
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
import java.util.HashMap;
import java.util.Map;

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
                                      Authentication authentication) throws IOException, ServletException {
        
        if (authentication instanceof OAuth2AuthenticationToken oauth2Token) {
            try {
                OAuth2User oauth2User = oauth2Token.getPrincipal();
                
                // Extract user information from Google OAuth2
                String googleId = oauth2User.getAttribute("sub");
                String email = oauth2User.getAttribute("email");
                String name = oauth2User.getAttribute("name");
                String avatar = oauth2User.getAttribute("picture");

                log.info("OAuth2 authentication success for user: {} ({})", name, email);

                // Create or update user in database
                User user = userService.createOrUpdateUser(googleId, email, name, avatar);

                // Generate JWT token
                String jwtToken = jwtService.generateToken(googleId, email, name);

                // Prepare response data
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("message", "Authentication successful");
                responseData.put("token", jwtToken);
                responseData.put("user", Map.of(
                    "googleId", user.getGoogleId(),
                    "email", user.getEmail(),
                    "name", user.getName(),
                    "avatar", user.getAvatar(),
                    "smartWalletAddress", user.getSmartWalletAddress()
                ));
                responseData.put("expiresIn", jwtService.getExpirationTime());

                // Set response headers
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.setHeader("Access-Control-Allow-Origin", frontendUrl);
                response.setHeader("Access-Control-Allow-Credentials", "true");

                // Send JSON response instead of redirect for API usage
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(objectMapper.writeValueAsString(responseData));

                log.info("JWT token generated and sent for user: {}", email);

            } catch (Exception e) {
                log.error("OAuth2 authentication success handling failed", e);
                sendErrorResponse(response, "Authentication processing failed");
            }
        } else {
            log.error("Invalid authentication type: {}", authentication.getClass().getSimpleName());
            sendErrorResponse(response, "Invalid authentication");
        }
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        errorResponse.put("error", "OAUTH2_ERROR");

        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", frontendUrl);
        
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}