package com.dsw02.empleados.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final byte[] secret;
    private final String issuer;
    private final long accessTokenSeconds;
    private final long refreshTokenSeconds;

    public JwtService(
        @Value("${app.auth.jwt-secret}") String secret,
        @Value("${app.auth.issuer}") String issuer,
        @Value("${app.auth.access-token-seconds:1800}") long accessTokenSeconds,
        @Value("${app.auth.refresh-token-seconds:43200}") long refreshTokenSeconds
    ) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.issuer = issuer;
        this.accessTokenSeconds = accessTokenSeconds;
        this.refreshTokenSeconds = refreshTokenSeconds;
    }

    public String createAccessToken(String subject, String role) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(accessTokenSeconds);
        return Jwts.builder()
            .subject(subject)
            .issuer(issuer)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .claims(Map.of("role", role, "type", "access"))
            .signWith(Keys.hmacShaKeyFor(secret))
            .compact();
    }

    public String createRefreshToken(String subject) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(refreshTokenSeconds);
        return Jwts.builder()
            .subject(subject)
            .issuer(issuer)
            .id(UUID.randomUUID().toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .claims(Map.of("type", "refresh"))
            .signWith(Keys.hmacShaKeyFor(secret))
            .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(secret))
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(parse(token).get("type", String.class));
    }

    public OffsetDateTime getExpiration(String token) {
        return OffsetDateTime.ofInstant(parse(token).getExpiration().toInstant(), ZoneOffset.UTC);
    }

    public long getAccessTokenSeconds() {
        return accessTokenSeconds;
    }

    public String sha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
