# Implementation Plan: CRUD de Empleados

**Branch**: `003-crud-empleados` | **Date**: 2026-02-26 | **Spec**: `/specs/003-crud-empleados/spec.md`
**Input**: Feature specification from `/specs/003-crud-empleados/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Implementar un API backend con CRUD completo de empleados protegido por HTTP Basic Auth,
usando `clave` como PK natural entera no autogenerada y validaciones obligatorias para
`nombre`, `dirección` y `teléfono` (no vacíos, máximo 100 caracteres). La solución seguirá
Spring Boot 3 + Java 17 + PostgreSQL en Docker, documentación OpenAPI/Swagger y cobertura
de pruebas de integración para autenticación, persistencia y contrato.

## Technical Context

<!--
  ACTION REQUIRED: Replace the content in this section with the technical details
  for the project. The structure here is presented in advisory capacity to guide
  the iteration process.
-->

**Language/Version**: Java 17  
**Primary Dependencies**: Spring Boot 3, Spring Security, Spring Data JPA, springdoc-openapi  
**Storage**: PostgreSQL (Docker-managed in local and CI)  
**Testing**: JUnit 5, Spring Boot Test, MockMvc/Testcontainers when applicable  
**Target Platform**: Linux server
**Project Type**: backend web-service  
**Performance Goals**: p95 < 2 segundos en operaciones CRUD válidas; creación válida en < 1 minuto en pruebas funcionales  
**Constraints**: HTTP Basic Auth, OpenAPI/Swagger required, Docker parity for DB  
**Scale/Scope**: MVP CRUD de una entidad (`Empleado`), 5 endpoints REST, volumen esperado bajo-medio (hasta ~10k registros en entorno de práctica)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- Stack gate: Plan uses Spring Boot 3 + Java 17 only.
- Security gate: Protected endpoints include HTTP Basic Auth design and test strategy.
- Data gate: PostgreSQL schema/data changes and Docker runtime impact are documented.
- Contract gate: OpenAPI changes and Swagger evidence are explicitly listed.
- Quality gate: Integration tests for auth, DB, and API contract are planned.

**Gate Status (Pre-Phase 0)**: PASS
- Stack gate: PASS (Java 17 + Spring Boot 3 definidos como base única).
- Security gate: PASS (todas las operaciones CRUD protegidas con Basic Auth; pruebas autorizadas/no autorizadas planificadas).
- Data gate: PASS (tabla `empleado` en PostgreSQL y ejecución Docker local/CI documentadas).
- Contract gate: PASS (contrato OpenAPI en `contracts/empleados.openapi.yaml` y Swagger obligatorio).
- Quality gate: PASS (pruebas de integración para auth, DB y contrato incluidas en alcance).

## Project Structure

### Documentation (this feature)

```text
specs/003-crud-empleados/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
src/main/java/
├── config/
├── controller/
├── service/
├── repository/
└── model/

src/main/resources/
├── application.yml
└── db/migration/

src/test/java/
├── unit/
├── integration/
└── contract/

docker/
└── docker-compose.yml
```

**Structure Decision**: Se adopta una estructura backend monolítica Spring Boot por capas
(`controller`, `service`, `repository`, `model`) para mantener separación clara de
responsabilidades. En el estado actual del repositorio sólo existen artefactos de especificación;
las rutas de `src/` y `docker/` se crearán en implementación (fase `/speckit.tasks`).

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [e.g., 4th project] | [current need] | [why 3 projects insufficient] |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient] |

## Post-Design Constitution Check

**Gate Status (Post-Phase 1)**: PASS
- Stack gate: PASS (diseño y contrato permanecen en Java 17/Spring Boot 3).
- Security gate: PASS (contrato define `basicAuth` global en endpoints CRUD).
- Data gate: PASS (modelo de datos y reglas de persistencia PostgreSQL explícitas).
- Contract gate: PASS (OpenAPI detallado con respuestas de éxito y error).
- Quality gate: PASS (quickstart incluye pruebas de integración y validación de contrato).
