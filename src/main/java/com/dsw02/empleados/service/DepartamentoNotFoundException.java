package com.dsw02.empleados.service;

public class DepartamentoNotFoundException extends RuntimeException {

    public DepartamentoNotFoundException(String id) {
        super("Departamento no encontrado para id: " + id);
    }
}
