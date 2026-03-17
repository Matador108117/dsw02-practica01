# Implementation Plan: Correccion de acceso global en Swagger UI

**Branch**: `001-swagger-global-auth` | **Date**: 2026-03-16 | **Spec**: `/home/matador1081/semestre6/deploy/dsw02-practica01/specs/001-swagger-global-auth/spec.md`
**Input**: Feature specification from `/home/matador1081/semestre6/deploy/dsw02-practica01/specs/001-swagger-global-auth/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Estandarizar el comportamiento de seguridad en Swagger UI para que todos los
endpoints protegidos queden bloqueados por defecto, la autenticacion sea
centralizada via boton Authorize y la sesion se reaplique automaticamente hasta
que el backend responda `401`. La correccion mantiene HTTP Basic global,
preserva contrato y rutas de API, no abre `v3`, y agrega bootstrap interno
idempotente del usuario de prueba obligatorio en `v2`.

## Technical Context

**Language/Version**: Java 17  
**Primary Dependencies**: Spring Boot 3, Spring Security, Spring Data JPA,
springdoc-openapi, Flyway, Jakarta Validation  
**Storage**: PostgreSQL (Docker-managed in local and CI)  
**Testing**: JUnit 5, Spring Boot Test, MockMvc/Testcontainers when applicable  
**Target Platform**: Linux server
**Project Type**: backend web-service  
**Performance Goals**:
- SC-001/SC-002: 100% de endpoints protegidos bloqueados antes de auth y 100%
  habilitados tras un unico Authorize valido.
- SC-003: 0 solicitudes adicionales de login por metodo mientras no haya `401`.
- SC-004a: 100% de flujos fuerzan re-Authorize despues del primer `401`.
- SC-005: 100% de autenticacion exitosa con usuario de prueba en entornos limpios
  y reiniciados.
**Constraints**: Mandatory HTTP Basic auth (`type=http`, `scheme=basic`) on API
methods using `correo_electronico` as username and transient `contrasena`
validated against persisted `contrasena_hash`, role-based access (`USER`
read-only, `ADMIN` CRUD), OpenAPI/Swagger required, Docker parity for DB,
API major versioning (`/api/v{major}`), paginated list endpoints,
bootstrap interno idempotente de usuario de prueba, y cambio restringido a `v2`
sin apertura de `v3`  
**Scale/Scope**:
- Ambito funcional: seguridad de Swagger UI y validacion backend en API de empleados.
- Ambito API: sin endpoints nuevos; se conserva contrato publico existente.
- Ambito datos: se permiten ajustes internos de persistencia y esquema para cumplir
  hash-only en credenciales y normalizacion idempotente del usuario de prueba,
  sin alterar contrato publico ni rutas API.
- Ambito versionado: cambios internos aplicados exclusivamente en `v2`.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- Stack gate: Plan uses Spring Boot 3 + Java 17 only.
- Security gate: Protected endpoints include mandatory HTTP Basic auth (`type=http`,
  `scheme=basic`) with `correo_electronico` username mapping, transient password
  verification against persisted hash, plus role policy (`USER` read-only,
  `ADMIN` CRUD) design and test strategy.
- Data gate: PostgreSQL schema/data changes and Docker runtime impact are documented.
- Employee data gate: Any feature touching `empleado` persistence documents and
  enforces required `correo_electronico` and `contrasena_hash` attributes, while
  keeping `contrasena` as input-only and not persisted in plaintext.
- Contract gate: OpenAPI changes and Swagger evidence are explicitly listed.
- Quality gate: Integration tests for auth, role authorization, DB, and API contract
  are planned.
- Versioning gate: API path version impact is documented (`/api/v{major}`) and
  breaking changes include migration notes.
- Sunset gate: Deprecated version cutoff behavior is documented and enforces
  `410 Gone` using UTC as business clock.
- Pagination gate: List endpoints define defaults, max limits, and validation tests.
- Workflow gate: Branch strategy, atomic commits, and PR traceability to spec/tasks
  are explicitly documented.

**Gate Result (pre-Phase 0)**:
- Stack gate: PASS.
- Security gate: PASS.
- Data gate: PASS (con cambios internos de esquema/persistencia acotados al
  cumplimiento de seguridad y bootstrap, sin impacto contractual publico).
- Employee data gate: PASS (se preservan campos obligatorios y hash-only).
- Contract gate: PASS (OpenAPI global security y evidencia Swagger documentadas).
- Quality gate: PASS (plan de pruebas de integracion y contrato definido).
- Versioning gate: PASS (`v2` se mantiene sin ruptura contractual).
- Sunset gate: PASS (no cambia politica de sunset vigente).
- Pagination gate: PASS (sin regresion sobre endpoints de coleccion).
- Workflow gate: PASS (rama dedicada y trazabilidad a spec).

## Project Structure

### Documentation (this feature)

```text
specs/001-swagger-global-auth/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)
```text
src/main/java/com/dsw02/empleados/
├── EmpleadosApplication.java
├── config/
│   ├── OpenApiConfig.java
│   └── SecurityConfig.java
├── controller/
├── model/
├── repository/
└── service/

src/main/resources/
├── application.yml
└── db/migration/

src/test/java/com/dsw02/empleados/
├── integration/
└── contract/

docker/
└── docker-compose.yml
```

**Structure Decision**: Mantener el monolito Spring Boot existente y aplicar la
correccion en capas de configuracion (`config`), seguridad HTTP y pruebas de
integracion/contrato, sin introducir nuevos modulos ni alterar el routing publico.

## Phase 0: Research Output

- Ver `/home/matador1081/semestre6/deploy/dsw02-practica01/specs/001-swagger-global-auth/research.md`.

## Phase 1: Design Output

- Ver `/home/matador1081/semestre6/deploy/dsw02-practica01/specs/001-swagger-global-auth/data-model.md`.
- Ver `/home/matador1081/semestre6/deploy/dsw02-practica01/specs/001-swagger-global-auth/contracts/swagger-global-auth-v2.openapi.yaml`.
- Ver `/home/matador1081/semestre6/deploy/dsw02-practica01/specs/001-swagger-global-auth/quickstart.md`.

## Post-Design Constitution Check

- Stack gate: PASS.
- Security gate: PASS.
- Data gate: PASS.
- Employee data gate: PASS.
- Contract gate: PASS.
- Quality gate: PASS.
- Versioning gate: PASS.
- Sunset gate: PASS.
- Pagination gate: PASS.
- Workflow gate: PASS.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | N/A | N/A |
