package com.dsw02.empleados.model;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Represents an authentication attempt for rate-limiting purposes.
 * Tracks failed attempts per correo_electronico and IP origin.
 * Implements brute-force protection with 15-minute windows.
 */
@Entity
@Table(name = "auth_attempt", 
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"correo_electronico", "ip_origen"},
        name = "uk_auth_attempt_correo_ip"
    ),
    indexes = {
        @Index(name = "idx_auth_attempt_blocked_until", columnList = "blocked_until"),
        @Index(name = "idx_auth_attempt_correo", columnList = "correo_electronico")
    }
)
public class AuthAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String correoElectronico;

    @Column(nullable = false, length = 45)
    private String ipOrigen;

    @Column(nullable = false)
    private Integer failedCountWindow;

    @Column(nullable = false)
    private OffsetDateTime windowStartAt;

    @Column(nullable = true)
    private OffsetDateTime blockedUntil;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    // Constructors
    public AuthAttempt() {}

    public AuthAttempt(String correoElectronico, String ipOrigen) {
        this.correoElectronico = correoElectronico;
        this.ipOrigen = ipOrigen;
        this.failedCountWindow = 0;
        this.windowStartAt = OffsetDateTime.now();
        this.blockedUntil = null;
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

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public String getIpOrigen() {
        return ipOrigen;
    }

    public void setIpOrigen(String ipOrigen) {
        this.ipOrigen = ipOrigen;
    }

    public Integer getFailedCountWindow() {
        return failedCountWindow;
    }

    public void setFailedCountWindow(Integer failedCountWindow) {
        this.failedCountWindow = failedCountWindow;
    }

    public OffsetDateTime getWindowStartAt() {
        return windowStartAt;
    }

    public void setWindowStartAt(OffsetDateTime windowStartAt) {
        this.windowStartAt = windowStartAt;
    }

    public OffsetDateTime getBlockedUntil() {
        return blockedUntil;
    }

    public void setBlockedUntil(OffsetDateTime blockedUntil) {
        this.blockedUntil = blockedUntil;
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

    @Override
    public String toString() {
        return "AuthAttempt{" +
                "id=" + id +
                ", correoElectronico='" + correoElectronico + '\'' +
                ", ipOrigen='" + ipOrigen + '\'' +
                ", failedCountWindow=" + failedCountWindow +
                ", windowStartAt=" + windowStartAt +
                ", blockedUntil=" + blockedUntil +
                '}';
    }
}
