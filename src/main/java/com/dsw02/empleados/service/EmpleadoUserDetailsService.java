package com.dsw02.empleados.service;

import com.dsw02.empleados.model.Empleado;
import com.dsw02.empleados.repository.EmpleadoRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Custom UserDetailsService for HTTP Basic Authentication.
 * Loads user details by correo_electronico (email) from the database.
 * Integrates with Spring Security for authentication and role-based authorization.
 */
@Service
public class EmpleadoUserDetailsService implements UserDetailsService {

    private final EmpleadoRepository empleadoRepository;

    public EmpleadoUserDetailsService(EmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    /**
     * Load user by correo_electronico (email).
     * Case-insensitive lookup per RFC 5321.
     *
     * @param correoElectronico the email address used as username in HTTP Basic Auth
     * @return UserDetails for Spring Security
     * @throws UsernameNotFoundException if no empleado exists with this email
     */
    @Override
    public UserDetails loadUserByUsername(String correoElectronico) throws UsernameNotFoundException {
        // Normalize email to lowercase for case-insensitive lookup
        String normalizedEmail = correoElectronico.toLowerCase();

        Empleado empleado = empleadoRepository
            .findByCorreoElectronicoIgnoreCase(normalizedEmail)
            .orElseThrow(() -> new UsernameNotFoundException(
                "Empleado not found with correo_electronico: " + correoElectronico));

        // Verify empleado is active
        if (!empleado.getActivo()) {
            throw new UsernameNotFoundException(
                "Empleado account is inactive: " + correoElectronico);
        }

        // Build authorities from role
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + empleado.getRol().name()));

        // Return Spring Security User with contrasena_hash for authentication
        return User.builder()
            .username(empleado.getCorreoElectronico())
            .password(empleado.getContrasenaHash())
            .authorities(authorities)
            .accountLocked(false)
            .disabled(!empleado.getActivo())
            .build();
    }
}
