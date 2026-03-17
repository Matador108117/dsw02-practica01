package com.dsw02.empleados.integration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.dsw02.empleados.controller.dto.EmpleadoDtos.EmpleadoCreateRequest;
import com.dsw02.empleados.model.Rol;
import com.dsw02.empleados.service.EmpleadoService;

@SpringBootTest
class EmpleadoCreateConcurrencyIT extends BasePostgresIT {

    @Autowired
    private EmpleadoService service;
    
    @Test
    void shouldGenerateUniqueKeysConcurrently() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(8);
        List<Callable<String>> tasks = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            int index = i;
            tasks.add(() -> service.create(new EmpleadoCreateRequest(
                "Nombre " + index,
                "Direccion " + index,
                "55000000" + index,
                "user" + index + "@example.com",
                "Password!" + index,
                Rol.USER
            )).clave());
        }

        List<Future<String>> futures = executor.invokeAll(tasks);
        executor.shutdown();

        Set<String> keys = new HashSet<>();
        for (Future<String> future : futures) {
            keys.add(future.get());
        }

        assertThat(keys).hasSize(12);
    }
}
