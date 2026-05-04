package com.dsw02.empleados.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 1, max = 255) String password
    ) {
    }

    public record LoginSuccessResponse(
        String status,
        String role
    ) {
    }

    public record RefreshSuccessResponse(
        String status,
        String role,
        int expiresIn
    ) {
    }

    public record AuthErrorResponse(
        String code,
        String message,
        String timestamp
    ) {
    }
}
