package com.dsw02.empleados.controller.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class DepartamentoDtos {

    private DepartamentoDtos() {
    }

    public record DepartamentoCreateRequest(
        @NotBlank @Size(max = 150) String nombre
    ) {
    }

    public record DepartamentoUpdateRequest(
        @NotBlank @Size(max = 150) String nombre
    ) {
    }

    public record DepartamentoResponse(
        String id,
        String nombre
    ) {
    }

    public record DepartamentoPageResponse(
        List<DepartamentoResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
    ) {
    }
}
