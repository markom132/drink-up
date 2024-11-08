package com.party_up.network.config.authentication;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import com.party_up.network.model.User;

/**
 * Utility class for handling JWT operations such as extracting claims and validating tokens.
 */
@Component
public class JwtUtil {

    // Secret key used for signing the JWTs
    private final SecretKey SECRET_KEY =
            Jwts.SIG.HS256.key().build();

    /**
     * Extracts the username (subject) from the given JWT token.
     *
     * @param token the JWT token from which to extract the username
     * @return the username (subject) extracted from the token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from the given JWT token.
     *
     * @param token the JWT token from which to extract the expiration date
     * @return the expiration date of the token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract a specific claim from the given JWT token.
     *
     * @param token          the JWT token from which to extract the claim
     * @param claimsResolver a function that defines how to extract the desired claim
     * @param <T>            the type of the claim to be extracted
     * @return the extracted claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token); // Extract all claims from the token
        return claimsResolver.apply(claims); // Apply the claim resolver function
    }

    /**
     * Extract all claims from the given JWT token.
     *
     * @param token the JWT token from which to extract claims
     * @return the claims contained within the token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY) // Set the signing key for validation
                .build().parseSignedClaims(token)
                .getPayload(); // Retrieve the claims body
    }

    /**
     * Checks if the given JWT token has expired.
     *
     * @param token the JWT token to check
     * @return true if the token is expired, false otherwise
     */
    private Boolean isTokenExpired(String token) {
        // Extract expiration date and compare it with the current date
        return extractExpiration(token).before(new Date());
    }

    /**
     * Generates a JWT token for the given user.
     *
     * @param userDetails the user details for which the token is generated
     * @return the generated token
     */
    public String generateToken(User userDetails) {
        Map<String, Object> claims = new HashMap<>(); // Create a map for claims
        return createToken(claims, userDetails.getUsername()); // Create a token with claims and subject
    }

    /**
     * Creates a JWT token with the specific claims and subject.
     *
     * @param claims  the claims to include in the token
     * @param subject the subject (username) for the token
     * @return the created JWT token as a string
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims) // Set claims in the token
                .subject(subject) // Set the subject
                .issuedAt(new Date(System.currentTimeMillis())) // Set the issued date
                // Set the expiration to 10 hours
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(SECRET_KEY) // Sign the token with the secret key
                .compact(); // Build the token
    }

    /**
     * Validates the given JWT token against the provided user details.
     *
     * @param token       the JWT token to validate
     * @param userDetails the user details to validate against
     * @return true if the token is valid, false otherwise
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token); // Extract username from the token
        // Check if the username matches and if the token is not expired
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

}