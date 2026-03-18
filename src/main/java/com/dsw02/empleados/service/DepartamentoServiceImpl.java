package com.dsw02.empleados.service;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dsw02.empleados.controller.dto.DepartamentoDtos.DepartamentoCreateRequest;
import com.dsw02.empleados.controller.dto.DepartamentoDtos.DepartamentoPageResponse;
import com.dsw02.empleados.controller.dto.DepartamentoDtos.DepartamentoResponse;
import com.dsw02.empleados.controller.dto.DepartamentoDtos.DepartamentoUpdateRequest;
import com.dsw02.empleados.controller.dto.EmpleadoDtos.EmpleadoPageResponse;
import com.dsw02.empleados.controller.dto.EmpleadoDtos.EmpleadoResponse;
import com.dsw02.empleados.model.Departamento;
import com.dsw02.empleados.model.Empleado;
import com.dsw02.empleados.repository.DepartamentoRepository;
import com.dsw02.empleados.repository.EmpleadoRepository;

@Service
public class DepartamentoServiceImpl implements DepartamentoService {

    private static final Pattern ID_PATTERN = Pattern.compile("^DEP-[0-9]{6}$");

    private final DepartamentoRepository departamentoRepository;
    private final EmpleadoRepository empleadoRepository;
    private final DepartamentoIdGenerator departamentoIdGenerator;
    private final ClaveEmpleadoFormatter claveEmpleadoFormatter;

    public DepartamentoServiceImpl(
        DepartamentoRepository departamentoRepository,
        EmpleadoRepository empleadoRepository,
        DepartamentoIdGenerator departamentoIdGenerator,
        ClaveEmpleadoFormatter claveEmpleadoFormatter
    ) {
        this.departamentoRepository = departamentoRepository;
        this.empleadoRepository = empleadoRepository;
        this.departamentoIdGenerator = departamentoIdGenerator;
        this.claveEmpleadoFormatter = claveEmpleadoFormatter;
    }

    @Override
    @Transactional
    public DepartamentoResponse create(DepartamentoCreateRequest request) {
        Departamento departamento = new Departamento();
        departamento.setId(departamentoIdGenerator.nextId());
        departamento.setNombre(request.nombre().trim());

        Departamento saved = departamentoRepository.save(departamento);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartamentoPageResponse findAll(Integer page, Integer size) {
        Pageable pageable = PaginationDefaults.normalize(page, size);
        Page<Departamento> result = departamentoRepository.findAll(pageable);
        List<DepartamentoResponse> content = result.getContent().stream().map(this::toResponse).toList();
        return new DepartamentoPageResponse(
            content,
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DepartamentoResponse findById(String id) {
        validateId(id);
        Departamento departamento = departamentoRepository.findById(id)
            .orElseThrow(() -> new DepartamentoNotFoundException(id));
        return toResponse(departamento);
    }

    @Override
    @Transactional
    public DepartamentoResponse update(String id, DepartamentoUpdateRequest request) {
        validateId(id);
        Departamento departamento = departamentoRepository.findById(id)
            .orElseThrow(() -> new DepartamentoNotFoundException(id));

        departamento.setNombre(request.nombre().trim());
        Departamento saved = departamentoRepository.save(departamento);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(String id) {
        validateId(id);
        Departamento departamento = departamentoRepository.findById(id)
            .orElseThrow(() -> new DepartamentoNotFoundException(id));

        if (empleadoRepository.existsByDepartamento_Id(id)) {
            throw new DepartamentoConflictException(id);
        }

        departamentoRepository.delete(departamento);
    }

    @Override
    @Transactional(readOnly = true)
    public EmpleadoPageResponse findEmpleadosByDepartamento(String id, Integer page, Integer size) {
        validateId(id);
        if (!departamentoRepository.existsById(id)) {
            throw new DepartamentoNotFoundException(id);
        }

        Pageable pageable = PaginationDefaults.normalize(page, size);
        Page<Empleado> result = empleadoRepository.findByDepartamento_Id(id, pageable);
        List<EmpleadoResponse> content = result.getContent().stream()
            .map(empleado -> new EmpleadoResponse(
                claveEmpleadoFormatter.buildClave(empleado.getId().getConsecutivo()),
                empleado.getId().getPrefijo(),
                empleado.getId().getConsecutivo(),
                empleado.getNombre(),
                empleado.getDireccion(),
                empleado.getTelefono(),
                empleado.getCorreoElectronico(),
                empleado.getRol().name(),
                empleado.getActivo(),
                empleado.getDepartamento() == null ? null : empleado.getDepartamento().getId()
            ))
            .toList();

        return new EmpleadoPageResponse(
            content,
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages()
        );
    }

    private void validateId(String id) {
        if (id == null || !ID_PATTERN.matcher(id).matches()) {
            throw new IllegalArgumentException("Formato de id de departamento invalido. Se espera DEP- + 6 digitos.");
        }
    }

    private DepartamentoResponse toResponse(Departamento departamento) {
        return new DepartamentoResponse(departamento.getId(), departamento.getNombre());
    }
}
