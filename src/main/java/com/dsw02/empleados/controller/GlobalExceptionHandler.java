package com.dsw02.empleados.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.dsw02.empleados.controller.dto.EmpleadoDtos.ErrorResponse;
import com.dsw02.empleados.service.DepartamentoConflictException;
import com.dsw02.empleados.service.DepartamentoNotFoundException;
import com.dsw02.empleados.service.EmpleadoNotFoundException;
import com.dsw02.empleados.service.InvalidDepartamentoReferenceException;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String message = fieldError == null ? "Datos inválidos" : fieldError.getField() + ": " + fieldError.getDefaultMessage();
        return ResponseEntity.badRequest().body(new ErrorResponse("VALIDATION_ERROR", message));
    }

    @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception exception) {
        return ResponseEntity.badRequest().body(new ErrorResponse("BAD_REQUEST", exception.getMessage()));
    }

    @ExceptionHandler(EmpleadoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EmpleadoNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("NOT_FOUND", exception.getMessage()));
    }

    @ExceptionHandler(DepartamentoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDepartamentoNotFound(DepartamentoNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("NOT_FOUND", exception.getMessage()));
    }

    @ExceptionHandler(DepartamentoConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(DepartamentoConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("CONFLICT", exception.getMessage()));
    }

    @ExceptionHandler(InvalidDepartamentoReferenceException.class)
    public ResponseEntity<ErrorResponse> handleUnprocessable(InvalidDepartamentoReferenceException exception) {
        return ResponseEntity.unprocessableEntity().body(new ErrorResponse("UNPROCESSABLE_ENTITY", exception.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse("AUTH_INVALID_CREDENTIALS", exception.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ErrorResponse("AUTH_FORBIDDEN", exception.getMessage()));
    }
}
