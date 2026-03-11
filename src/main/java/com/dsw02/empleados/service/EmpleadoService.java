package com.dsw02.empleados.service;

import com.dsw02.empleados.controller.dto.EmpleadoDtos.EmpleadoCreateRequest;
import com.dsw02.empleados.controller.dto.EmpleadoDtos.EmpleadoResponse;
import com.dsw02.empleados.controller.dto.EmpleadoDtos.EmpleadoUpdateRequest;
import java.util.List;

public interface EmpleadoService {

    EmpleadoResponse create(EmpleadoCreateRequest request);

    List<EmpleadoResponse> findAll();

    EmpleadoResponse findByClave(String clave);

    EmpleadoResponse update(String clave, EmpleadoUpdateRequest request);

    void delete(String clave);
}
