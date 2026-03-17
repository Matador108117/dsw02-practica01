package com.dsw02.empleados.config;

import com.dsw02.empleados.model.ClaveEmpleadoId;
import com.dsw02.empleados.model.Empleado;
import com.dsw02.empleados.model.Rol;
import com.dsw02.empleados.repository.EmpleadoRepository;
import com.dsw02.empleados.service.ApiVersionSupportPolicyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.time.OffsetDateTime;

/**
 * Spring Security configuration for HTTP Basic Authentication with role-based authorization.
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", 
                    "/swagger-ui.css", "/swagger-ui-*.js", "/swagger-ui-*.css").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public Object bootstrapAdminUser() {
        try {
            boolean adminExists = empleadoRepository.findAll().stream()
                .anyMatch(e -> e.getRol() == Rol.ADMIN);

            if (adminExists && onlyBootstrapOnce) {
                log.info("ADMIN user already exists, skipping bootstrap");
                return new Object();
            }

            log.info("Creating bootstrap ADMIN user: {}", bootstrapAdminEmail);
            PasswordEncoder encoder = passwordEncoder();
            String hashedPassword = encoder.encode(bootstrapAdminPassword);

            Empleado admin = empleadoRepository
                .findByCorreoElectronicoIgnoreCase(bootstrapAdminEmail)
                .orElse(new Empleado());

            admin.setId(new ClaveEmpleadoId("EMP-", 1L));
            admin.setNombre("Bootstrap Admin");
            admin.setDireccion("--");
            admin.setTelefono("--");
            admin.setCorreoElectronico(bootstrapAdminEmail);
            admin.setContrasenaHash(hashedPassword);
            admin.setRol(Rol.ADMIN);
            admin.setActivo(true);

            empleadoRepository.save(admin);
            log.info("Bootstrap ADMIN created successfully");

            OffsetDateTime releaseV2 = OffsetDateTime.parse(releaseV2UtcString);
            OffsetDateTime sunsetV1 = OffsetDateTime.parse(sunsetV1UtcString);
            versionPolicyService.initializeDefaultPolicyIfNeeded(
                "empleados", "v1", "v2", releaseV2, sunsetV1
            );
            log.info("API version policy initialized");

        } catch (Exception e) {
            log.error("Failed to bootstrap ADMIN user", e);
            throw new RuntimeException("Bootstrap ADMIN initialization failed", e);
        }
        return new Object();
    }
}
