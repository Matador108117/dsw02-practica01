package com.dsw02.empleados.model;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * Represents API version support policy for runtime sunset enforcement.
 * Controls coexistence of v1 (deprecated) and v2 (active) endpoints with time-bound enforcement.
 */
@Entity
@Table(name = "api_version_support_policy",
    indexes = @Index(name = "idx_api_version_support_policy_name", columnList = "api_name", unique = true)
)
public class ApiVersionSupportPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "api_name", nullable = false, length = 100, unique = true)
    private String apiName;

    @Column(name = "deprecated_version", nullable = false, length = 10)
    private String deprecatedVersion;

    @Column(name = "active_version", nullable = false, length = 10)
    private String activeVersion;

    @Column(name = "deprecation_notice", columnDefinition = "TEXT")
    private String deprecationNotice;

    @Column(name = "release_v2_at_utc", nullable = false)
    private OffsetDateTime releaseV2AtUtc;

    @Column(name = "sunset_at_utc", nullable = false)
    private OffsetDateTime sunsetAtUtc;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // Constructors
    public ApiVersionSupportPolicy() {}

    public ApiVersionSupportPolicy(String apiName, String deprecatedVersion, String activeVersion,
                                  OffsetDateTime releaseV2AtUtc, OffsetDateTime sunsetAtUtc) {
        this.apiName = apiName;
        this.deprecatedVersion = deprecatedVersion;
        this.activeVersion = activeVersion;
        this.releaseV2AtUtc = releaseV2AtUtc;
        this.sunsetAtUtc = sunsetAtUtc;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getDeprecatedVersion() {
        return deprecatedVersion;
    }

    public void setDeprecatedVersion(String deprecatedVersion) {
        this.deprecatedVersion = deprecatedVersion;
    }

    public String getActiveVersion() {
        return activeVersion;
    }

    public void setActiveVersion(String activeVersion) {
        this.activeVersion = activeVersion;
    }

    public String getDeprecationNotice() {
        return deprecationNotice;
    }

    public void setDeprecationNotice(String deprecationNotice) {
        this.deprecationNotice = deprecationNotice;
    }

    public OffsetDateTime getReleaseV2AtUtc() {
        return releaseV2AtUtc;
    }

    public void setReleaseV2AtUtc(OffsetDateTime releaseV2AtUtc) {
        this.releaseV2AtUtc = releaseV2AtUtc;
    }

    public OffsetDateTime getSunsetAtUtc() {
        return sunsetAtUtc;
    }

    public void setSunsetAtUtc(OffsetDateTime sunsetAtUtc) {
        this.sunsetAtUtc = sunsetAtUtc;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Check if a version is expired post-sunset
     */
    public boolean isVersionSunset(OffsetDateTime currentTime) {
        return currentTime.isAfter(this.sunsetAtUtc) || currentTime.isEqual(this.sunsetAtUtc);
    }

    @Override
    public String toString() {
        return "ApiVersionSupportPolicy{" +
                "id=" + id +
                ", apiName='" + apiName + '\'' +
                ", deprecatedVersion='" + deprecatedVersion + '\'' +
                ", activeVersion='" + activeVersion + '\'' +
                ", releaseV2AtUtc=" + releaseV2AtUtc +
                ", sunsetAtUtc=" + sunsetAtUtc +
                '}';
    }
}
