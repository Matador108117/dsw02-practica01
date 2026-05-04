package com.dsw02.empleados.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dsw02.empleados.controller.dto.EmpleadoDtos.EmpleadoCreateRequest;
import com.dsw02.empleados.controller.dto.EmpleadoDtos.EmpleadoPageResponse;
import com.dsw02.empleados.controller.dto.EmpleadoDtos.EmpleadoResponse;
import com.dsw02.empleados.controller.dto.EmpleadoDtos.EmpleadoUpdateRequest;
import com.dsw02.empleados.model.ClaveEmpleadoId;
import com.dsw02.empleados.model.Departamento;
import com.dsw02.empleados.model.Empleado;
import com.dsw02.empleados.model.Rol;
import com.dsw02.empleados.repository.DepartamentoRepository;
import com.dsw02.empleados.repository.EmpleadoRepository;

@Service
public class EmpleadoServiceImpl implements EmpleadoService {

    private static final Logger log = LoggerFactory.getLogger(EmpleadoServiceImpl.class);

    private final EmpleadoRepository repository;
    private final ClaveEmpleadoFormatter formatter;
    private final ClaveEmpleadoParser parser;
    private final PasswordEncoder passwordEncoder;
    private final DepartamentoRepository departamentoRepository;

    public EmpleadoServiceImpl(
        EmpleadoRepository repository,
        ClaveEmpleadoFormatter formatter,
        ClaveEmpleadoParser parser,
        PasswordEncoder passwordEncoder,
        DepartamentoRepository departamentoRepository
    ) {
        this.repository = repository;
        this.formatter = formatter;
        this.parser = parser;
        this.passwordEncoder = passwordEncoder;
        this.departamentoRepository = departamentoRepository;
    }

    @Override
    @Transactional
    public EmpleadoResponse create(EmpleadoCreateRequest request) {
        long consecutivo = repository.nextConsecutivo();
        ClaveEmpleadoId id = new ClaveEmpleadoId(ClaveEmpleadoFormatter.PREFIJO, consecutivo);

        Empleado empleado = new Empleado();
        empleado.setId(id);
        empleado.setNombre(request.nombre().trim());
        empleado.setDireccion(request.direccion().trim());
        empleado.setTelefono(request.telefono().trim());
        
        // Normalize email to lowercase for case-insensitive uniqueness
        String normalizedEmail = request.correoElectronico().trim().toLowerCase();
        empleado.setCorreoElectronico(normalizedEmail);
        
        // Hash password and never store plaintext
        String hashedPassword = passwordEncoder.encode(request.contrasena());
        empleado.setContrasenaHash(hashedPassword);
        
        // Set role (default to USER if not provided)
        empleado.setRol(request.rol() != null ? request.rol() : Rol.USER);
        empleado.setActivo(true);
        empleado.setDepartamento(resolveDepartamento(request.departamentoId()));

        Empleado saved = repository.save(empleado);
        EmpleadoResponse response = toResponse(saved);

        log.info("event=ALTA clave={} prefijo={} consecutivo={}", response.clave(), response.prefijo(), response.consecutivo());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public EmpleadoPageResponse findAll(Integer page, Integer size) {
        Pageable normalized = PaginationDefaults.normalize(page, size);
        Pageable pageable = PageRequest.of(
            normalized.getPageNumber(),
            normalized.getPageSize(),
            Sort.by(Sort.Direction.ASC, "id.consecutivo")
        );
        Page<Empleado> result = repository.findAll(pageable);
        List<EmpleadoResponse> content = result.getContent().stream().map(this::toResponse).toList();
        return new EmpleadoPageResponse(
            content,
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public EmpleadoResponse findByClave(String clave) {
        ClaveEmpleadoId id = parser.parse(clave);
        Empleado empleado = repository.findById(id).orElseThrow(() -> new EmpleadoNotFoundException(clave));
        return toResponse(empleado);
    }

    @Override
    @Transactional
    public EmpleadoResponse update(String clave, EmpleadoUpdateRequest request) {
        ClaveEmpleadoId id = parser.parse(clave);
        Empleado empleado = repository.findById(id).orElseThrow(() -> new EmpleadoNotFoundException(clave));

        if (request.nombre() != null) {
            empleado.setNombre(request.nombre().trim());
        }
        if (request.direccion() != null) {
            empleado.setDireccion(request.direccion().trim());
        }
        if (request.telefono() != null) {
            empleado.setTelefono(request.telefono().trim());
        }
        if (request.correoElectronico() != null) {
            empleado.setCorreoElectronico(request.correoElectronico().trim().toLowerCase());
        }
        // Only update password if provided
        if (request.contrasena() != null && !request.contrasena().isEmpty()) {
            String hashedPassword = passwordEncoder.encode(request.contrasena());
            empleado.setContrasenaHash(hashedPassword);
        }
        if (request.rol() != null) {
            empleado.setRol(request.rol());
        }
        if (request.activo() != null) {
            empleado.setActivo(request.activo());
        }
        if (request.departamentoId() != null) {
            empleado.setDepartamento(resolveDepartamento(request.departamentoId()));
        }

        Empleado saved = repository.save(empleado);
        EmpleadoResponse response = toResponse(saved);
        log.info("event=ACTUALIZACION clave={} prefijo={} consecutivo={}", response.clave(), response.prefijo(), response.consecutivo());
        return response;
    }

    @Override
    @Transactional
    public void delete(String clave) {
        ClaveEmpleadoId id = parser.parse(clave);
        Empleado empleado = repository.findById(id).orElseThrow(() -> new EmpleadoNotFoundException(clave));
        repository.delete(empleado);
        log.info("event=ELIMINACION clave={}", clave);
    }

    private EmpleadoResponse toResponse(Empleado empleado) {
        Long consecutivo = empleado.getId().getConsecutivo();
        String prefijo = empleado.getId().getPrefijo();
        return new EmpleadoResponse(
            formatter.buildClave(consecutivo),
            prefijo,
            consecutivo,
            empleado.getNombre(),
            empleado.getDireccion(),
            empleado.getTelefono(),
            empleado.getCorreoElectronico(),
            empleado.getRol().name(),
            empleado.getActivo(),
            empleado.getDepartamento() == null ? null : empleado.getDepartamento().getId()
        );
    }

    private Departamento resolveDepartamento(String departamentoId) {
        if (departamentoId == null || departamentoId.isBlank()) {
            return null;
        }

        return departamentoRepository.findById(departamentoId)
            .orElseThrow(() -> new InvalidDepartamentoReferenceException(departamentoId));
    }
}
