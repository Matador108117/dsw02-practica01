package com.dsw02.empleados.service;

import com.dsw02.empleados.model.ClaveEmpleadoId;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class ClaveEmpleadoParser {

    private static final Pattern PATTERN = Pattern.compile("^EMP-(\\d{6,})$");

    public ClaveEmpleadoId parse(String clave) {
        Matcher matcher = PATTERN.matcher(clave);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Formato de clave inválido. Se espera EMP- + dígitos.");
        }

        long consecutivo = Long.parseLong(matcher.group(1));
        return new ClaveEmpleadoId(ClaveEmpleadoFormatter.PREFIJO, consecutivo);
    }
}
