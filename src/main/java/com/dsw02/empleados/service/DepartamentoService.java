package com.dsw02.empleados.service;

import com.dsw02.empleados.controller.dto.DepartamentoDtos.DepartamentoCreateRequest;
import com.dsw02.empleados.controller.dto.DepartamentoDtos.DepartamentoPageResponse;
import com.dsw02.empleados.controller.dto.DepartamentoDtos.DepartamentoResponse;
import com.dsw02.empleados.controller.dto.DepartamentoDtos.DepartamentoUpdateRequest;
import com.dsw02.empleados.controller.dto.EmpleadoDtos.EmpleadoPageResponse;

public interface DepartamentoService {

    DepartamentoResponse create(DepartamentoCreateRequest request);

    DepartamentoPageResponse findAll(Integer page, Integer size);

    DepartamentoResponse findById(String id);

    DepartamentoResponse update(String id, DepartamentoUpdateRequest request);

    void delete(String id);

    EmpleadoPageResponse findEmpleadosByDepartamento(String id, Integer page, Integer size);
}
