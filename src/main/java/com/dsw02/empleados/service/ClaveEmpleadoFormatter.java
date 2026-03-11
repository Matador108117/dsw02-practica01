package com.dsw02.empleados.service;

import org.springframework.stereotype.Component;

@Component
public class ClaveEmpleadoFormatter {

    public static final String PREFIJO = "EMP-";

    public String buildClave(Long consecutivo) {
        return PREFIJO + String.format("%06d", consecutivo);
    }
}
