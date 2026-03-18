package com.dsw02.empleados.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dsw02.empleados.model.ApiVersionSupportPolicy;
import com.dsw02.empleados.repository.ApiVersionSupportPolicyRepository;

@ExtendWith(MockitoExtension.class)
class ApiVersionSupportPolicyServiceTest {

    @Mock
    private ApiVersionSupportPolicyRepository policyRepository;

    private ApiVersionSupportPolicyService service;

    @BeforeEach
    void setUp() {
        service = new ApiVersionSupportPolicyService(policyRepository);
    }

    @Test
    void shouldReturnTrueWhenVersionIsAlreadySunsetInUtc() {
        ApiVersionSupportPolicy policy = new ApiVersionSupportPolicy(
            "empleados",
            "v1",
            "v2",
            OffsetDateTime.now(ZoneOffset.UTC).minusDays(30),
            OffsetDateTime.now(ZoneOffset.UTC).minusDays(1)
        );
        when(policyRepository.findByApiName("empleados")).thenReturn(Optional.of(policy));

        assertThat(service.isVersionSunset("empleados")).isTrue();
    }

    @Test
    void shouldReturnFalseWhenVersionIsStillBeforeSunsetInUtc() {
        ApiVersionSupportPolicy policy = new ApiVersionSupportPolicy(
            "empleados",
            "v1",
            "v2",
            OffsetDateTime.now(ZoneOffset.UTC).minusDays(30),
            OffsetDateTime.now(ZoneOffset.UTC).plusDays(1)
        );
        when(policyRepository.findByApiName("empleados")).thenReturn(Optional.of(policy));

        assertThat(service.isVersionSunset("empleados")).isFalse();
    }

    @Test
    void shouldReturnZeroSecondsWhenSunsetAlreadyPassed() {
        ApiVersionSupportPolicy policy = new ApiVersionSupportPolicy(
            "empleados",
            "v1",
            "v2",
            OffsetDateTime.now(ZoneOffset.UTC).minusDays(30),
            OffsetDateTime.now(ZoneOffset.UTC).minusHours(1)
        );
        when(policyRepository.findByApiName("empleados")).thenReturn(Optional.of(policy));

        assertThat(service.getSecondsUntilSunset("empleados")).isEqualTo(0L);
    }
}
