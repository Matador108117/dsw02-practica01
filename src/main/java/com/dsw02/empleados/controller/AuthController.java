package com.dsw02.empleados.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dsw02.empleados.controller.dto.AuthDtos.LoginRequest;
import com.dsw02.empleados.controller.dto.AuthDtos.LoginSuccessResponse;
import com.dsw02.empleados.controller.dto.AuthDtos.RefreshSuccessResponse;
import com.dsw02.empleados.service.AuthServiceImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v4/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthServiceImpl authService;

    public AuthController(AuthServiceImpl authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            LoginSuccessResponse body = authService.login(request, response);
            return ResponseEntity.ok(body);
        } catch (Exception ex) {
            log.warn("Auth login failed for email {}: {}", request.email(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(authService.buildAuthError("AUTH_INVALID_CREDENTIALS", "Invalid email or password"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        try {
            RefreshSuccessResponse body = authService.refresh(request, response);
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException ex) {
            if ("CSRF validation failed".equals(ex.getMessage())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(authService.buildAuthError("AUTH_CSRF_INVALID", ex.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(authService.buildAuthError("AUTH_REFRESH_INVALID", ex.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            authService.logout(request, response);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(authService.buildAuthError("AUTH_CSRF_INVALID", ex.getMessage()));
        }
    }
}
