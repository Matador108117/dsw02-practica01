package com.dsw02.empleados.service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dsw02.empleados.model.AuthAttempt;
import com.dsw02.empleados.repository.AuthAttemptRepository;

/**
 * Service for managing authentication attempts and brute-force rate-limiting.
 * Implements per-email+IP lockout: 5 failed attempts per 15-minute window,
 * followed by 15-minute lockout on the email+IP pair.
 */
@Service
@Transactional
public class AuthAttemptService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int WINDOW_MINUTES = 15;
    private static final int LOCKOUT_MINUTES = 15;

    private final AuthAttemptRepository authAttemptRepository;

    public AuthAttemptService(AuthAttemptRepository authAttemptRepository) {
        this.authAttemptRepository = authAttemptRepository;
    }

    /**
     * Record a failed authentication attempt.
     * If max attempts exceeded, lock the email+IP for LOCKOUT_MINUTES.
     *
     * @param correoElectronico the email address
     * @param ipOrigen the origin IP address
     * @return true if locked after this failure, false otherwise
     */
    public boolean recordFailedAttempt(String correoElectronico, String ipOrigen) {
        OffsetDateTime now = OffsetDateTime.now();

        AuthAttempt attempt = authAttemptRepository
            .findByCorreoElectronicAndIpOrigen(correoElectronico, ipOrigen)
            .orElse(new AuthAttempt(correoElectronico, ipOrigen));

        // Check if current window has expired
        long minutesSinceWindowStart = ChronoUnit.MINUTES.between(attempt.getWindowStartAt(), now);
        if (minutesSinceWindowStart >= WINDOW_MINUTES) {
            // Reset window
            attempt.setWindowStartAt(now);
            attempt.setFailedCountWindow(1);
            attempt.setBlockedUntil(null);
        } else {
            // Increment failed count
            attempt.setFailedCountWindow(attempt.getFailedCountWindow() + 1);
        }

        // Check if max attempts exceeded
        if (attempt.getFailedCountWindow() >= MAX_FAILED_ATTEMPTS) {
            // Lock out for LOCKOUT_MINUTES
            OffsetDateTime lockoutUntil = now.plus(LOCKOUT_MINUTES, ChronoUnit.MINUTES);
            attempt.setBlockedUntil(lockoutUntil);
        }

        attempt.setUpdatedAt(now);
        authAttemptRepository.save(attempt);

        return attempt.getBlockedUntil() != null && attempt.getBlockedUntil().isAfter(now);
    }

    /**
     * Check if authentication is currently blocked for email+IP pair.
     *
     * @param correoElectronico the email address
     * @param ipOrigen the origin IP address
     * @return true if locked, false otherwise
     */
    public boolean isBlocked(String correoElectronico, String ipOrigen) {
        return authAttemptRepository
            .findByCorreoElectronicAndIpOrigen(correoElectronico, ipOrigen)
            .map(attempt -> {
                if (attempt.getBlockedUntil() != null) {
                    return attempt.getBlockedUntil().isAfter(OffsetDateTime.now());
                }
                return false;
            })
            .orElse(false);
    }

    /**
     * Reset failed attempt count on successful authentication.
     *
     * @param correoElectronico the email address
     * @param ipOrigen the origin IP address
     */
    public void resetAttempts(String correoElectronico, String ipOrigen) {
        authAttemptRepository
            .findByCorreoElectronicAndIpOrigen(correoElectronico, ipOrigen)
            .ifPresent(attempt -> {
                attempt.setFailedCountWindow(0);
                attempt.setBlockedUntil(null);
                attempt.setWindowStartAt(OffsetDateTime.now());
                attempt.setUpdatedAt(OffsetDateTime.now());
                authAttemptRepository.save(attempt);
            });
    }

    /**
     * Cleanup expired blocks (administrative operation).
     */
    public void cleanupExpiredBlocks() {
        authAttemptRepository.deleteExpiredBlocks(OffsetDateTime.now());
    }
}
