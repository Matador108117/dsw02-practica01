# Implementation Plan: CRUD de Empleados con Clave Compuesta

**Branch**: `001-actualizar-clave-compuesta` | **Date**: 2026-02-26 | **Spec**: `/specs/001-actualizar-clave-compuesta/spec.md`
**Input**: Feature specification from `/specs/001-actualizar-clave-compuesta/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Modificar el CRUD de empleados para que la identidad use clave compuesta con prefijo fijo
`EMP-` y consecutivo numérico autogenerado, manteniendo validaciones de campos de texto,
autenticación básica, persistencia PostgreSQL en Docker y contrato OpenAPI/Swagger actualizado.
La estrategia técnica usa Spring Boot 3 + Java 17, generación transaccional de consecutivo
sin colisiones y pruebas de integración para autenticación, base de datos y contrato.

## Technical Context

**Language/Version**: Java 17  
**Primary Dependencies**: Spring Boot 3, Spring Security, Spring Data JPA, springdoc-openapi  
**Storage**: PostgreSQL (Docker-managed in local and CI)  
**Testing**: JUnit 5, Spring Boot Test, MockMvc/Testcontainers when applicable  
**Target Platform**: Linux server
**Project Type**: backend web-service  
**Performance Goals**: p95 < 2 segundos en operaciones CRUD válidas; 0 colisiones de clave en pruebas concurrentes de alta  
**Constraints**: HTTP Basic Auth, OpenAPI/Swagger required, Docker parity for DB  
**Scale/Scope**: MVP de una entidad (`Empleado`) con 5 endpoints REST y volumen bajo-medio (~10k registros de práctica)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- Stack gate: Plan uses Spring Boot 3 + Java 17 only.
- Security gate: Protected endpoints include HTTP Basic Auth design and test strategy.
- Data gate: PostgreSQL schema/data changes and Docker runtime impact are documented.
- Contract gate: OpenAPI changes and Swagger evidence are explicitly listed.
- Quality gate: Integration tests for auth, DB, and API contract are planned.

**Gate Status (Pre-Phase 0)**: PASS
- Stack gate: PASS (solo Java 17 y Spring Boot 3).
- Security gate: PASS (CRUD protegido por HTTP Basic Auth con pruebas de autorizado/no autorizado).
- Data gate: PASS (modelo PostgreSQL y ejecución Docker documentados).
- Contract gate: PASS (contrato OpenAPI planificado en `contracts/empleados.openapi.yaml`).
- Quality gate: PASS (pruebas de integración de auth, DB y contrato incluidas).

## Project Structure

### Documentation (this feature)

```text
specs/001-actualizar-clave-compuesta/
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

**Structure Decision**: Se adopta una estructura monolítica Spring por capas (`controller`,
`service`, `repository`, `model`) para aislar reglas de generación de clave compuesta y facilitar
pruebas de integración. En este repositorio, las rutas de código fuente se crearán en implementación;
esta fase mantiene artefactos de diseño en `specs/001-actualizar-clave-compuesta/`.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | N/A | N/A |

## Post-Design Constitution Check

**Gate Status (Post-Phase 1)**: PASS
- Stack gate: PASS (diseño sin desviaciones de Java 17/Spring Boot 3).
- Security gate: PASS (contrato define seguridad `basicAuth` para endpoints CRUD).
- Data gate: PASS (modelo de datos compuesta + reglas de unicidad/concurrencia especificadas).
- Contract gate: PASS (OpenAPI detalla requests/responses/errores y seguridad).
- Quality gate: PASS (quickstart define pruebas de integración para auth, DB y contrato).
