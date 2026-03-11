package com.dsw02.empleados.controller;

import com.dsw02.empleados.controller.dto.EmpleadoDtos.EmpleadoCreateRequest;
import com.dsw02.empleados.controller.dto.EmpleadoDtos.EmpleadoResponse;
import com.dsw02.empleados.controller.dto.EmpleadoDtos.EmpleadoUpdateRequest;
import com.dsw02.empleados.service.EmpleadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/empleados")
@SecurityRequirement(name = "basicAuth")
public class EmpleadoController {

    private final EmpleadoService service;

    public EmpleadoController(EmpleadoService service) {
        this.service = service;
    }

    @Operation(summary = "Registrar empleado (clave autogenerada)")
    @ApiResponse(responseCode = "201", description = "Empleado creado", content = @Content(schema = @Schema(implementation = EmpleadoResponse.class)))
    @ApiResponse(responseCode = "400", description = "Error de validación")
    @PostMapping
    public ResponseEntity<EmpleadoResponse> create(@Valid @RequestBody EmpleadoCreateRequest request) {
        EmpleadoResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/empleados/" + response.clave())).body(response);
    }

    @Operation(summary = "Listar empleados")
    @ApiResponse(responseCode = "200", description = "Lista de empleados", content = @Content(array = @ArraySchema(schema = @Schema(implementation = EmpleadoResponse.class))))
    @GetMapping
    public List<EmpleadoResponse> findAll() {
        return service.findAll();
    }

    @Operation(summary = "Consultar empleado por clave compuesta")
    @ApiResponse(responseCode = "200", description = "Empleado encontrado", content = @Content(schema = @Schema(implementation = EmpleadoResponse.class)))
    @ApiResponse(responseCode = "404", description = "Empleado no encontrado")
    @GetMapping("/{clave}")
    public EmpleadoResponse findByClave(@PathVariable String clave) {
        return service.findByClave(clave);
    }

    @Operation(summary = "Actualizar empleado por clave compuesta")
    @ApiResponse(responseCode = "200", description = "Empleado actualizado", content = @Content(schema = @Schema(implementation = EmpleadoResponse.class)))
    @ApiResponse(responseCode = "404", description = "Empleado no encontrado")
    @PutMapping("/{clave}")
    public EmpleadoResponse update(@PathVariable String clave, @Valid @RequestBody EmpleadoUpdateRequest request) {
        return service.update(clave, request);
    }

    @Operation(summary = "Eliminar empleado por clave compuesta")
    @ApiResponse(responseCode = "204", description = "Empleado eliminado")
    @ApiResponse(responseCode = "404", description = "Empleado no encontrado")
    @DeleteMapping("/{clave}")
    public ResponseEntity<Void> delete(@PathVariable String clave) {
        service.delete(clave);
        return ResponseEntity.noContent().build();
    }
}
