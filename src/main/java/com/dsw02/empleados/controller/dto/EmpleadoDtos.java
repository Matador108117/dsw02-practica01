package com.dsw02.empleados.controller.dto;

import java.util.List;

import com.dsw02.empleados.model.Rol;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class EmpleadoDtos {

    private EmpleadoDtos() {
    }

    /**
     * Create request: Requires contrasena (transitional input only, not persisted as plaintext).
     * rol is optional (defaults to USER if not provided).
     */
    public record EmpleadoCreateRequest(
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Size(max = 100) String direccion,
        @NotBlank @Size(max = 100) String telefono,
        @NotBlank @Email @Size(max = 150) String correoElectronico,
        @NotBlank @Size(min = 8, max = 255) String contrasena,
        Rol rol,  // Optional, defaults to USER if null
        @Size(max = 10) String departamentoId
    ) {
    }

    /**
     * Update request: Optionally includes new contrasena.
     * If provided, it will be hashed and persisted. If omitted, existing hash is retained.
     */
    public record EmpleadoUpdateRequest(
        @Size(max = 100) String nombre,
        @Size(max = 100) String direccion,
        @Size(max = 100) String telefono,
        @Email @Size(max = 150) String correoElectronico,
        @Size(min = 8, max = 255) String contrasena,  // Optional; NULL means keep existing hash
        Rol rol,
        Boolean activo,
        @Size(max = 10) String departamentoId
    ) {
    }

    /**
     * Response: Never exposes contrasena or contrasena_hash.
     * Includes rol and activo for authorization and status checks.
     */
    public record EmpleadoResponse(
        String clave,
        String prefijo,
        Long consecutivo,
        String nombre,
        String direccion,
        String telefono,
        String correoElectronico,
        String rol,
        Boolean activo,
        String departamentoId
    ) {
    }

    public record EmpleadoPageResponse(
        List<EmpleadoResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
    ) {
    }

    public record ErrorResponse(String code, String message) {
    }
}
