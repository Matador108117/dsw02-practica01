package com.dsw02.empleados.service;

import com.dsw02.empleados.controller.dto.EmpleadoDtos.EmpleadoCreateRequest;
import com.dsw02.empleados.controller.dto.EmpleadoDtos.EmpleadoPageResponse;
import com.dsw02.empleados.controller.dto.EmpleadoDtos.EmpleadoResponse;
import com.dsw02.empleados.controller.dto.EmpleadoDtos.EmpleadoUpdateRequest;

public interface EmpleadoService {

    EmpleadoResponse create(EmpleadoCreateRequest request);

    EmpleadoPageResponse findAll(Integer page, Integer size);

    EmpleadoResponse findByClave(String clave);

    EmpleadoResponse update(String clave, EmpleadoUpdateRequest request);

    void delete(String clave);
}
