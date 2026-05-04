package com.dsw02.empleados.model;

/**
 * Role enum for role-based access control.
 * ADMIN: Full CRUD operations allowed.
 * USER: Read-only operations only.
 */
public enum Rol {
    ADMIN,   // Full CRUD access
    USER     // Read-only access
}
