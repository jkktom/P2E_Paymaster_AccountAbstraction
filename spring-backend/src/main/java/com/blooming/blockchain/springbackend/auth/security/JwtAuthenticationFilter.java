package com.blooming.blockchain.springbackend.auth.security;

import com.blooming.blockchain.springbackend.auth.jwt.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String googleId;

        // Check if Authorization header exists and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract JWT token
        jwt = authHeader.substring(7);
        
        try {
            googleId = jwtService.extractGoogleId(jwt);

            // If token is valid and user is not already authenticated
            if (googleId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                if (jwtService.validateToken(jwt)) {
                    // Extract user details from JWT
                    String email = jwtService.extractEmail(jwt);
                    String name = jwtService.extractName(jwt);

                    // Create UserDetails object
                    UserDetails userDetails = User.builder()
                            .username(googleId)
                            .password("") // No password needed for JWT auth
                            .authorities(Collections.emptyList()) // Add roles if needed later
                            .build();

                    // Create authentication token
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails, 
                            null, 
                            userDetails.getAuthorities()
                        );

                    // Add additional details to the authentication
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // Set the authentication in SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    // Add custom attributes to request for controller access
                    request.setAttribute("googleId", googleId);
                    request.setAttribute("email", email);
                    request.setAttribute("name", name);

                    log.debug("JWT authentication successful for user: {}", googleId);
                } else {
                    log.debug("Invalid JWT token for user: {}", googleId);
                }
            }
        } catch (Exception e) {
            log.error("JWT authentication failed: {}", e.getMessage());
            // Clear security context on error
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        // Skip JWT filter for OAuth2 endpoints and public endpoints
        return path.startsWith("/oauth2/") || 
               path.startsWith("/login/oauth2/") ||
               path.equals("/auth/user") ||
               path.startsWith("/public/") ||
               path.startsWith("/actuator/health");
    }
}