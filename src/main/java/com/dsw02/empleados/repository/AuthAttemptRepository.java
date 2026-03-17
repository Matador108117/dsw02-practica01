package com.dsw02.empleados.repository;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dsw02.empleados.model.AuthAttempt;

/**
 * Repository for AuthAttempt entity.
 * Provides data access for rate-limiting and brute-force protection.
 */
@Repository
public interface AuthAttemptRepository extends JpaRepository<AuthAttempt, Long> {

    /**
     * Find authentication attempt by correo_electronico and IP origin
     */
    Optional<AuthAttempt> findByCorreoElectronicAndIpOrigen(String correoElectronico, String ipOrigen);

    /**
     * Clean up blocked entries after they expire
     */
    @Query("DELETE FROM AuthAttempt a WHERE a.blockedUntil IS NOT NULL AND a.blockedUntil < :now")
    void deleteExpiredBlocks(@Param("now") OffsetDateTime now);

    /**
     * Count active blocks for an email
     */
    @Query("SELECT COUNT(a) FROM AuthAttempt a WHERE a.correoElectronico = :correo AND a.blockedUntil > :now")
    long countActiveBlocks(@Param("correo") String correo, @Param("now") OffsetDateTime now);
}
