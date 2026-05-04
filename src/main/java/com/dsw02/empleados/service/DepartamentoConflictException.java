package com.dsw02.empleados.service;

public class DepartamentoConflictException extends RuntimeException {

    public DepartamentoConflictException(String id) {
        super("No se puede eliminar el departamento " + id + " porque tiene empleados asociados");
    }
}
