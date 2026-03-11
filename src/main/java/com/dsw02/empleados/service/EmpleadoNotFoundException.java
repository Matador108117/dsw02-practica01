package com.dsw02.empleados.service;

public class EmpleadoNotFoundException extends RuntimeException {

    public EmpleadoNotFoundException(String clave) {
        super("Empleado no encontrado para clave: " + clave);
    }
}
