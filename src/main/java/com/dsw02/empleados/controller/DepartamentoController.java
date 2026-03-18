package com.dsw02.empleados.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dsw02.empleados.controller.dto.DepartamentoDtos.DepartamentoCreateRequest;
import com.dsw02.empleados.controller.dto.DepartamentoDtos.DepartamentoPageResponse;
import com.dsw02.empleados.controller.dto.DepartamentoDtos.DepartamentoResponse;
import com.dsw02.empleados.controller.dto.DepartamentoDtos.DepartamentoUpdateRequest;
import com.dsw02.empleados.controller.dto.EmpleadoDtos.EmpleadoPageResponse;
import com.dsw02.empleados.service.DepartamentoService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v3/departamentos")
@SecurityRequirement(name = "basicAuth")
public class DepartamentoController {

    private final DepartamentoService departamentoService;

    public DepartamentoController(DepartamentoService departamentoService) {
        this.departamentoService = departamentoService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<DepartamentoResponse> create(@Valid @RequestBody DepartamentoCreateRequest request) {
        DepartamentoResponse response = departamentoService.create(request);
        return ResponseEntity.created(URI.create("/api/v3/departamentos/" + response.id())).body(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping
    public DepartamentoPageResponse findAll(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size
    ) {
        return departamentoService.findAll(page, size);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/{id}")
    public DepartamentoResponse findById(@PathVariable String id) {
        return departamentoService.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public DepartamentoResponse update(@PathVariable String id, @Valid @RequestBody DepartamentoUpdateRequest request) {
        return departamentoService.update(id, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        departamentoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/{id}/empleados")
    public EmpleadoPageResponse findEmpleados(
        @PathVariable String id,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size
    ) {
        return departamentoService.findEmpleadosByDepartamento(id, page, size);
    }
}
