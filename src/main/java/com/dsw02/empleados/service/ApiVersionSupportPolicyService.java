package com.dsw02.empleados.service;

import com.dsw02.empleados.model.ApiVersionSupportPolicy;
import com.dsw02.empleados.repository.ApiVersionSupportPolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Service for managing API version support policies.
 * Enforces v1 deprecation and sunset with UTC-based time enforcement.
 */
@Service
@Transactional
public class ApiVersionSupportPolicyService {

    private final ApiVersionSupportPolicyRepository policyRepository;

    public ApiVersionSupportPolicyService(ApiVersionSupportPolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    /**
     * Find version policy by API name.
     *
     * @param apiName the API name (e.g., "empleados")
     * @return Optional containing the policy if found
     */
    public Optional<ApiVersionSupportPolicy> findByApiName(String apiName) {
        return policyRepository.findByApiName(apiName);
    }

    /**
     * Check if a version is currently sunsetted.
     * A version is sunsetted if current UTC time >= sunset_at_utc.
     *
     * @param apiName the API name
     * @return true if version is sunsetted, false if still in deprecation window
     */
    public boolean isVersionSunset(String apiName) {
        return findByApiName(apiName)
            .map(policy -> policy.isVersionSunset(OffsetDateTime.now()))
            .orElse(false);
    }

    /**
     * Get remaining time until sunset in seconds.
     * Returns 0 if already sunsetted, negative if past sunset.
     *
     * @param apiName the API name
     * @return seconds remaining, or -1 if policy not found
     */
    public long getSecondsUntilSunset(String apiName) {
        return findByApiName(apiName)
            .map(policy -> {
                long secondsRemaining = ChronoUnit.SECONDS.between(
                    OffsetDateTime.now(),
                    policy.getSunsetAtUtc()
                );
                return Math.max(0, secondsRemaining);
            })
            .orElse(-1L);
    }

    /**
     * Initialize default policy for empleados API if not exists.
     * Called during application startup to ensure policy is available.
     */
    public void initializeDefaultPolicyIfNeeded(
            String apiName,
            String deprecatedVersion,
            String activeVersion,
            OffsetDateTime releaseV2AtUtc,
            OffsetDateTime sunsetAtUtc) {

        if (policyRepository.findByApiName(apiName).isEmpty()) {
            ApiVersionSupportPolicy policy = new ApiVersionSupportPolicy(
                apiName,
                deprecatedVersion,
                activeVersion,
                releaseV2AtUtc,
                sunsetAtUtc
            );
            policy.setDeprecationNotice("Version " + deprecatedVersion + " deprecated as of " + releaseV2AtUtc + ", will be removed on " + sunsetAtUtc);
            policyRepository.save(policy);
        }
    }
}

// Utility import
import java.time.temporal.ChronoUnit;
