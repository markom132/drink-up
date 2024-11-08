package com.party_up.network.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

import com.party_up.network.model.AuthToken;

/**
 * Repository interface for managing {@link AuthToken} entities.
 * Provides methods for performing CRUD operations and custom queries.
 */
@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

    /**
     * Finds an {@link AuthToken} by its token string.
     *
     * @param token the token string to search for
     * @return an {@link Optional} containing the found {@link AuthToken}, or empty if not found
     */
    Optional<AuthToken> findByToken(String token);

    /**
     * Deletes all {@link AuthToken} entities that have an expiration date before specified date.
     *
     * @param dateTime the date before which tokens will be deleted
     * @return the number of tokens deleted
     */
    @Transactional
    int deleteByExpiresAtBefore(LocalDateTime dateTime);
}
