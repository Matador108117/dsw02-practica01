package com.dsw02.empleados.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dsw02.empleados.model.ClaveEmpleadoId;
import com.dsw02.empleados.model.Empleado;

public interface EmpleadoRepository extends JpaRepository<Empleado, ClaveEmpleadoId> {

    @Query(value = "SELECT nextval('empleado_consecutivo_seq')", nativeQuery = true)
    Long nextConsecutivo();

    /**
     * Find empleado by correo_electronico with case-insensitive search.
     * Uses lowercase comparison per RFC 5321 email standards.
     *
     * @param correoElectronico the email address (case-insensitive)
     * @return Optional containing the empleado if found
     */
    @Query("SELECT e FROM Empleado e WHERE LOWER(e.correoElectronico) = LOWER(:correo)")
    Optional<Empleado> findByCorreoElectronicoIgnoreCase(@Param("correo") String correoElectronico);
}
