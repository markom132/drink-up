package com.party_up.network.config.authentication;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import io.micrometer.common.lang.NonNull;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.party_up.network.model.AuthToken;
import com.party_up.network.repository.AuthTokenRepository;

/**
 * JWT request filter for validating JWT tokens in incoming requests.
 * This filter extracts the JWT from the request header, validates it,
 * and sets the authentication in the security context if valid.
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;

    private final JwtUtil jwtUtil;

    private final AuthTokenRepository authTokenRepository;

    @Autowired
    public JwtRequestFilter(UserDetailsService userDetailsService,
                            JwtUtil jwtUtil,
                            AuthTokenRepository authTokenRepository) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.authTokenRepository = authTokenRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        // Extract the Authorization header from the request
        final String authorizationHeader = request.getHeader("Authorization");

        // Check if the Authorization header is present and starts with "Bearer "
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("JWT token is missing or invalid");
            return; // Exit the method
        }

        String username;
        String jwtToken = authorizationHeader.substring(7); // Extract the JWT token

        // Validate the JWT and handle potential exceptions
        try {
            username = jwtUtil.extractUsername(jwtToken); // Extract username (email) from the token
        } catch (ExpiredJwtException | SecurityException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);  // Correct HTTP status code
            response.getWriter().write("Expired JWT token");  // Ensure the message matches the error type
            return;
        }

        // If the email is valid and no authentication exists in the security context
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username); // Load user details

            // Validate the JWT token against the suer details
            if (jwtUtil.validateToken(jwtToken, userDetails)) {
                Optional<AuthToken> authTokenOptional = authTokenRepository.findByToken(jwtToken);

                // Check if the JWT token exists in the repository
                if (authTokenOptional.isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().write("JWT token not found");
                    return; // Exit the method
                } else {
                    AuthToken authToken = authTokenOptional.get();

                    // Check if the token has expired
                    if (authToken.getExpiresAt().isBefore(LocalDateTime.now())) {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        response.getWriter().write("JWT token has expired");
                        return; // Exit the method
                    }

                    // Create an authentication token and set it in the security context
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                    // Update the last used time for the token
                    authToken.setLastUsedAt(LocalDateTime.now());
                    authTokenRepository.save(authToken); // Save the updated token information
                }
            }
        }

        // Proceed with the filter chain
        filterChain.doFilter(request, response);
    }
}