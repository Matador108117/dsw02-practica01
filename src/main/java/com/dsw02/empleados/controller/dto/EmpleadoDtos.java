package com.dsw02.empleados.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class EmpleadoDtos {

    private EmpleadoDtos() {
    }

    public record EmpleadoCreateRequest(
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Size(max = 100) String direccion,
        @NotBlank @Size(max = 100) String telefono
    ) {
    }

    public record EmpleadoUpdateRequest(
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Size(max = 100) String direccion,
        @NotBlank @Size(max = 100) String telefono
    ) {
    }

    public record EmpleadoResponse(
        String clave,
        String prefijo,
        Long consecutivo,
        String nombre,
        String direccion,
        String telefono
    ) {
    }

    public record ErrorResponse(String code, String message) {
    }
}
