package com.dsw02.empleados.service;

import org.springframework.stereotype.Component;

import com.dsw02.empleados.repository.DepartamentoRepository;

@Component
public class DepartamentoIdGenerator {

    private static final String PREFIJO = "DEP-";

    private final DepartamentoRepository departamentoRepository;

    public DepartamentoIdGenerator(DepartamentoRepository departamentoRepository) {
        this.departamentoRepository = departamentoRepository;
    }

    public String nextId() {
        long consecutivo = departamentoRepository.nextConsecutivo();
        return PREFIJO + String.format("%06d", consecutivo);
    }
}
