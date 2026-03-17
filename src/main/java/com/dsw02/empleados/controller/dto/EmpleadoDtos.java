package com.dsw02.empleados.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class EmpleadoDtos {

    private EmpleadoDtos() {
    }

    public record EmpleadoCreateRequest(
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Size(max = 100) String direccion,
        @NotBlank @Size(max = 100) String telefono,
        @NotBlank @Email @Size(max = 150) String correoElectronico,
        @NotBlank @Size(min = 8, max = 255) String contrasena
    ) {
    }

    public record EmpleadoUpdateRequest(
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Size(max = 100) String direccion,
        @NotBlank @Size(max = 100) String telefono,
        @NotBlank @Email @Size(max = 150) String correoElectronico,
        @NotBlank @Size(min = 8, max = 255) String contrasena
    ) {
    }

    public record EmpleadoResponse(
        String clave,
        String prefijo,
        Long consecutivo,
        String nombre,
        String direccion,
        String telefono,
        String correoElectronico
    ) {
    }

    public record ErrorResponse(String code, String message) {
    }
}
