package com.dsw02.empleados.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.dsw02.empleados.controller.dto.AuthDtos.AuthErrorResponse;
import com.dsw02.empleados.controller.dto.AuthDtos.LoginRequest;
import com.dsw02.empleados.controller.dto.AuthDtos.LoginSuccessResponse;
import com.dsw02.empleados.controller.dto.AuthDtos.RefreshSuccessResponse;
import com.dsw02.empleados.model.Empleado;
import com.dsw02.empleados.model.RefreshTokenSession;
import com.dsw02.empleados.repository.EmpleadoRepository;
import com.dsw02.empleados.repository.RefreshTokenSessionRepository;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;

@Service
public class AuthServiceImpl {

    private final AuthenticationManager authenticationManager;
    private final EmpleadoRepository empleadoRepository;
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
    private final JwtService jwtService;
    private final AuthCookieService authCookieService;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           EmpleadoRepository empleadoRepository,
                           RefreshTokenSessionRepository refreshTokenSessionRepository,
                           JwtService jwtService,
                           AuthCookieService authCookieService) {
        this.authenticationManager = authenticationManager;
        this.empleadoRepository = empleadoRepository;
        this.refreshTokenSessionRepository = refreshTokenSessionRepository;
        this.jwtService = jwtService;
        this.authCookieService = authCookieService;
    }

    @Transactional
    public LoginSuccessResponse login(LoginRequest request, HttpServletResponse response) {
        try {
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
            String email = auth.getName();
            Empleado empleado = empleadoRepository.findByCorreoElectronicoIgnoreCase(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

            String role = empleado.getRol().name();
            String empleadoClave = empleado.getId().getPrefijo() + empleado.getId().getConsecutivo();
            revokeActiveSessions(empleadoClave);

            String accessToken = jwtService.createAccessToken(email, role);
            String refreshToken = jwtService.createRefreshToken(email);
            String csrfToken = UUID.randomUUID().toString();

            RefreshTokenSession session = new RefreshTokenSession();
            session.setEmpleadoClave(empleadoClave);
            session.setRefreshTokenHash(jwtService.sha256(refreshToken));
            session.setCsrfToken(csrfToken);
            session.setExpiresAt(jwtService.getExpiration(refreshToken));
            session.setRevoked(false);
            session.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            refreshTokenSessionRepository.save(session);

            authCookieService.writeAuthCookies(
                response,
                accessToken,
                refreshToken,
                csrfToken,
                jwtService.getAccessTokenSeconds(),
                session.getExpiresAt().toEpochSecond() - OffsetDateTime.now(ZoneOffset.UTC).toEpochSecond()
            );

            return new LoginSuccessResponse("ACCEPTED", role);
        } catch (BadCredentialsException ex) {
            throw ex;
        }
    }

    public RefreshSuccessResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = readCookie(request, "REFRESH_TOKEN");
        String csrfCookie = readCookie(request, "XSRF-TOKEN");
        String csrfHeader = request.getHeader("X-CSRF-Token");

        if (refreshToken == null || csrfCookie == null || csrfHeader == null || !csrfCookie.equals(csrfHeader)) {
            throw new IllegalArgumentException("CSRF validation failed");
        }
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String refreshHash = jwtService.sha256(refreshToken);
        RefreshTokenSession session = refreshTokenSessionRepository
            .findByRefreshTokenHashAndRevokedFalse(refreshHash)
            .orElseThrow(() -> new IllegalArgumentException("Refresh token invalid or revoked"));

        if (!csrfCookie.equals(session.getCsrfToken()) || session.getExpiresAt().isBefore(OffsetDateTime.now(ZoneOffset.UTC))) {
            session.setRevoked(true);
            refreshTokenSessionRepository.save(session);
            throw new IllegalArgumentException("Refresh token expired or invalid");
        }

        Claims claims = jwtService.parse(refreshToken);
        Empleado empleado = empleadoRepository.findByCorreoElectronicoIgnoreCase(claims.getSubject())
            .orElseThrow(() -> new IllegalArgumentException("Subject not found"));

        String role = empleado.getRol().name();
        String accessToken = jwtService.createAccessToken(claims.getSubject(), role);
        authCookieService.writeAuthCookies(
            response,
            accessToken,
            refreshToken,
            session.getCsrfToken(),
            jwtService.getAccessTokenSeconds(),
            session.getExpiresAt().toEpochSecond() - OffsetDateTime.now(ZoneOffset.UTC).toEpochSecond()
        );

        return new RefreshSuccessResponse("ACCEPTED", role, (int) jwtService.getAccessTokenSeconds());
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = readCookie(request, "REFRESH_TOKEN");
        if (refreshToken != null) {
            refreshTokenSessionRepository.findByRefreshTokenHashAndRevokedFalse(jwtService.sha256(refreshToken))
                .ifPresent(session -> {
                    session.setRevoked(true);
                    refreshTokenSessionRepository.save(session);
                });
        }
        authCookieService.clearAuthCookies(response);
    }

    public AuthErrorResponse buildAuthError(String code, String message) {
        return new AuthErrorResponse(code, message, OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }

    private void revokeActiveSessions(String empleadoClave) {
        List<RefreshTokenSession> sessions = refreshTokenSessionRepository.findByEmpleadoClaveAndRevokedFalse(empleadoClave);
        for (RefreshTokenSession session : sessions) {
            session.setRevoked(true);
        }
        refreshTokenSessionRepository.saveAll(sessions);
        refreshTokenSessionRepository.deleteByRevokedTrueOrExpiresAtBefore(OffsetDateTime.now(ZoneOffset.UTC));
    }

    private String readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
