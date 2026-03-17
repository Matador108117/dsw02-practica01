package com.dsw02.empleados.config;

import com.dsw02.empleados.model.ClaveEmpleadoId;
import com.dsw02.empleados.model.Empleado;
import com.dsw02.empleados.model.Rol;
import com.dsw02.empleados.repository.EmpleadoRepository;
import com.dsw02.empleados.service.ApiVersionSupportPolicyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import com.dsw02.empleados.service.EmpleadoUserDetailsService;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

/**
 * Spring Security configuration for HTTP Basic Authentication with role-based authorization.
 * - Authentication: HTTP Basic using correo_electronico as username
 * - Password: BCryptPasswordEncoder with work factor 12
 * - Authorization: Role-based (ADMIN=CRUD, USER=read-only)
 * - Bootstrap: Automatic ADMIN creation on first run (one-time, secure)
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@Slf4j
public class SecurityConfig {

    private final EmpleadoRepository empleadoRepository;
    private final ApiVersionSupportPolicyService versionPolicyService;

    @Value("${app.bootstrap-admin.email:admin@empresa.com}")
    private String bootstrapAdminEmail;

    @Value("${app.bootstrap-admin.password:Admin123!}")
    private String bootstrapAdminPassword;

    @Value("${app.bootstrap-admin.only-once:true}")
    private boolean onlyBootstrapOnce;

    @Value("${app.api-version.release-v2-utc:2026-03-14T00:00:00Z}")
    private String releaseV2UtcString;

    @Value("${app.api-version.sunset-v1-utc:2026-06-12T00:00:00Z}")
    private String sunsetV1UtcString;

    public SecurityConfig(EmpleadoRepository empleadoRepository, 
                         ApiVersionSupportPolicyService versionPolicyService) {
        this.empleadoRepository = empleadoRepository;
        this.versionPolicyService = versionPolicyService;
    }

    /**
     * BCryptPasswordEncoder with work factor 12.
     * Cost factor 12 chosen for internal API with lower CPU overhead
     * compared to Argon2 (future upgrade path available)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * HTTP Basic authentication filter chain.
     * Permits public endpoints (swagger/openapi), requires auth for all others.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/swagger-ui.css", "/swagger-ui-*.js", "/swagger-ui-*.css").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    /**
     * DAO Authentication Provider with custom UserDetailsService and PasswordEncoder.
     * Loads user from database and validates password hash with constant-time comparison.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Authentication Manager for external use (e.g., in authentication entrypoint handlers).
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Bootstrap ADMIN user on first application run.
     * Creates single admin account if:
     * 1. app.bootstrap-admin.only-once = true AND no ADMIN exists, OR
     * 2. app.bootstrap-admin.only-once = false (always recreate)
     *
     * ADMIN bootstrapped with:
     * - Random enforced password change on first login (future enhancement)
     * - Audit logging of bootstrap event
     * - Single bootstrap to prevent accidental multiple ADMINs
     */
    @Bean
    public Object bootstrapAdminUser() {
        try {
            boolean adminExists = empleadoRepository.findAll().stream()
                .anyMatch(e -> e.getRol() == Rol.ADMIN);

            if (adminExists && onlyBootstrapOnce) {
                log.info("ADMIN user already exists and bootstrap-admin.only-once=true, skipping bootstrap");
                return new Object();
            }

            // Create or update bootstrap ADMIN
            log.info("Creating bootstrap ADMIN user: {}", bootstrapAdminEmail);

            PasswordEncoder encoder = passwordEncoder();
            String hashedPassword = encoder.encode(bootstrapAdminPassword);

            Empleado admin = empleadoRepository
                .findByCorreoElectronicoIgnoreCase(bootstrapAdminEmail)
                .orElse(new Empleado());

            // Set/update admin fields
            admin.setId(new ClaveEmpleadoId("EMP-", 1L));  // Reserved ID for bootstrap admin
            admin.setNombre("Bootstrap Admin");
            admin.setDireccion("--");
            admin.setTelefono("--");
            admin.setCorreoElectronico(bootstrapAdminEmail);
            admin.setContrasenaHash(hashedPassword);
            admin.setRol(Rol.ADMIN);
            admin.setActivo(true);

            empleadoRepository.save(admin);
            log.info("Bootstrap ADMIN created/updated successfully");

            // Initialize API version policy
            OffsetDateTime releaseV2 = OffsetDateTime.parse(releaseV2UtcString);
            OffsetDateTime sunsetV1 = OffsetDateTime.parse(sunsetV1UtcString);
            versionPolicyService.initializeDefaultPolicyIfNeeded(
                "empleados", "v1", "v2", releaseV2, sunsetV1
            );
            log.info("API version policy initialized: v1 deprecated {}, sunset {}", releaseV2, sunsetV1);

        } catch (Exception e) {
            log.error("Failed to bootstrap ADMIN user", e);
            throw new RuntimeException("Bootstrap ADMIN initialization failed", e);
        }
        return new Object();
    }
}
