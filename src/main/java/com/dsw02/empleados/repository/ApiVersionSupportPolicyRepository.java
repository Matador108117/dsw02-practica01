package com.dsw02.empleados.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dsw02.empleados.model.ApiVersionSupportPolicy;

/**
 * Repository for ApiVersionSupportPolicy entity.
 * Provides runtime access to version lifecycle policies.
 */
@Repository
public interface ApiVersionSupportPolicyRepository extends JpaRepository<ApiVersionSupportPolicy, Long> {

    /**
     * Find version support policy by API name
     */
    Optional<ApiVersionSupportPolicy> findByApiName(String apiName);
}
