package com.dsw02.empleados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.dsw02.empleados.model.Departamento;

public interface DepartamentoRepository extends JpaRepository<Departamento, String> {

    @Query(value = "SELECT nextval('departamento_consecutivo_seq')", nativeQuery = true)
    Long nextConsecutivo();
}
