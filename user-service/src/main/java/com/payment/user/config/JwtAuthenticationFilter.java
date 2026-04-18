package com.payment.user.config;

import com.payment.user.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter — Intercepts every HTTP request.
 * 
 * Flow:
 * 1. Check for 'Authorization: Bearer <token>' header
 * 2. Extract and validate the JWT token
 * 3. Load user details and set Spring Security context
 * 4. If no valid token, the request continues unauthenticated
 *    (security config decides whether to allow or deny)
 * 
 * This filter runs ONCE per request (OncePerRequestFilter).
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Extract Authorization header
        final String authHeader = request.getHeader("Authorization");

        // Skip if no Bearer token is present
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extract token (remove "Bearer " prefix)
            final String jwt = authHeader.substring(7);
            final String userEmail = jwtService.extractUsername(jwt);

            // Only authenticate if not already authenticated in this request context
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // Create authentication token and set it in the security context
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null, // No credentials needed (token-based)
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("Authenticated user: {} with roles: {}",
                            userEmail, userDetails.getAuthorities());
                }
            }
        } catch (Exception ex) {
            // Don't block the request on token errors — let security config handle unauthorized
            log.warn("JWT authentication failed: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
