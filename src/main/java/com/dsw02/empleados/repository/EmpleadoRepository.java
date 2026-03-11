package com.dsw02.empleados.repository;

import com.dsw02.empleados.model.ClaveEmpleadoId;
import com.dsw02.empleados.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EmpleadoRepository extends JpaRepository<Empleado, ClaveEmpleadoId> {

    @Query(value = "SELECT nextval('empleado_consecutivo_seq')", nativeQuery = true)
    Long nextConsecutivo();
}
