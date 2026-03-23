package com.dsw02.empleados.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dsw02.empleados.model.RefreshTokenSession;

public interface RefreshTokenSessionRepository extends JpaRepository<RefreshTokenSession, Long> {

    Optional<RefreshTokenSession> findByRefreshTokenHashAndRevokedFalse(String refreshTokenHash);

    List<RefreshTokenSession> findByEmpleadoClaveAndRevokedFalse(String empleadoClave);

    int deleteByRevokedTrueOrExpiresAtBefore(OffsetDateTime cutoff);
}
