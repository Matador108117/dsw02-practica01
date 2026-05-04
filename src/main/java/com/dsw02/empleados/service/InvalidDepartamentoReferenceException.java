package com.dsw02.empleados.service;

public class InvalidDepartamentoReferenceException extends RuntimeException {

    public InvalidDepartamentoReferenceException(String id) {
        super("departamentoId inexistente: " + id);
    }
}
