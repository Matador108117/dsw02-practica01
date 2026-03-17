package com.dsw02.empleados.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dsw02.empleados.controller.dto.EmpleadoDtos.EmpleadoCreateRequest;
import com.dsw02.empleados.controller.dto.EmpleadoDtos.EmpleadoResponse;
import com.dsw02.empleados.controller.dto.EmpleadoDtos.EmpleadoUpdateRequest;
import com.dsw02.empleados.model.ClaveEmpleadoId;
import com.dsw02.empleados.model.Empleado;
import com.dsw02.empleados.repository.EmpleadoRepository;

@Service
public class EmpleadoServiceImpl implements EmpleadoService {

    private static final Logger log = LoggerFactory.getLogger(EmpleadoServiceImpl.class);

    private final EmpleadoRepository repository;
    private final ClaveEmpleadoFormatter formatter;
    private final ClaveEmpleadoParser parser;

    public EmpleadoServiceImpl(
        EmpleadoRepository repository,
        ClaveEmpleadoFormatter formatter,
        ClaveEmpleadoParser parser
    ) {
        this.repository = repository;
        this.formatter = formatter;
        this.parser = parser;
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
        empleado.setCorreoElectronico(request.correoElectronico().trim());
        empleado.setContrasena(request.contrasena());

        Empleado saved = repository.save(empleado);
        EmpleadoResponse response = toResponse(saved);

        log.info("event=ALTA clave={} prefijo={} consecutivo={}", response.clave(), response.prefijo(), response.consecutivo());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmpleadoResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
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

        empleado.setNombre(request.nombre().trim());
        empleado.setDireccion(request.direccion().trim());
        empleado.setTelefono(request.telefono().trim());
        empleado.setCorreoElectronico(request.correoElectronico().trim());
        empleado.setContrasena(request.contrasena());

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
            empleado.getCorreoElectronico()
        );
    }
}
