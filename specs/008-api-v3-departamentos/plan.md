# Implementation Plan: API v3 Departamentos y Relacion Empleados

**Branch**: `008-api-v3-departamentos` | **Date**: 2026-03-17 | **Spec**: `/specs/008-api-v3-departamentos/spec.md`
**Input**: Feature specification from `/specs/008-api-v3-departamentos/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Introducir oficialmente API `v3` para el dominio de empleados con incorporacion
de la entidad persistente `Departamento`, relacion `Departamento (1) -> (N)
Empleado` mediante FK explicita y endpoint relacional obligatorio
`GET /api/v3/departamentos/{id}/empleados`. El alcance incluye CRUD de
departamentos, extension de POST/PUT de empleados para `departamento_id`
nullable, validacion previa de existencia con semantica `422` para referencias
invalidas, politica de eliminacion con `409` cuando existan dependencias,
paginacion de colecciones (`page=0`, `size=25`, `maxSize=100`), contrato
OpenAPI v3 y migraciones Flyway con backfill seguro.

## Technical Context

**Language/Version**: Java 17  
**Primary Dependencies**: Spring Boot 3, Spring Security, Spring Data JPA, springdoc-openapi  
**Storage**: PostgreSQL (Docker-managed in local and CI)  
**Testing**: JUnit 5, Spring Boot Test, MockMvc/Testcontainers when applicable  
**Target Platform**: Linux server
**Project Type**: backend web-service  
**Performance Goals**:
- P95 <= 800 ms para `GET /api/v3/departamentos` con `size=25` en entorno local de referencia.
- P95 <= 1000 ms para `GET /api/v3/departamentos/{id}/empleados` con hasta 100 empleados por pagina.
- 100% de solicitudes con `departamento_id` inexistente rechazadas con `422` sin escrituras parciales.
**Constraints**: Mandatory HTTP Basic auth (`type=http`, `scheme=basic`) on API
methods using `correo_electronico` as username and transient `contrasena`
validated against persisted `contrasena_hash`, role-based access (`USER`
read-only, `ADMIN` CRUD), OpenAPI/Swagger required, Docker parity for DB,
API major versioning (`/api/v{major}`), mandatory major bump when public contract
changes or new public endpoints are introduced, paginated list endpoints,
explicit FK-backed relational integrity for `Departamento (1) -> (N) Empleados`
with nullable employee assignment, feature-branch + PR workflow  
**Scale/Scope**:
- Nuevos recursos publicos: `/api/v3/departamentos` (CRUD) y `/api/v3/departamentos/{id}/empleados`.
- Ajustes en recurso existente: endpoints de empleados en `v3` aceptan `departamento_id` nullable en POST/PUT.
- Volumen objetivo inicial: hasta 10k empleados y 500 departamentos en entorno productivo esperado.
- Paginacion obligatoria en listados de coleccion con `size` maximo de 100.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- Stack gate: PASS. Plan uses Spring Boot 3 + Java 17 only.
- Security gate: PASS. Protected endpoints include mandatory HTTP Basic auth (`type=http`,
  `scheme=basic`) with `correo_electronico` username mapping, transient password
  verification against persisted hash, plus role policy (`USER` read-only,
  `ADMIN` CRUD) design and test strategy.
- Data gate: PASS. PostgreSQL schema/data changes and Docker runtime impact are documented.
- Employee data gate: PASS. Any feature touching `empleado` persistence documents and
  enforces required `correo_electronico` and `contrasena_hash` attributes, while
  keeping `contrasena` as input-only and not persisted in plaintext.
- Contract gate: PASS. OpenAPI changes and Swagger evidence are explicitly listed.
- Quality gate: PASS. Integration tests for auth, role authorization, DB, and API contract
  are planned.
- Versioning gate: PASS. API path version impact is documented (`/api/v{major}`) and
  breaking changes include migration notes.
- Contract evolution gate: PASS. Public contract expansion or new endpoints explicitly
  trigger major version increment (for Department-domain rollout: `v3` or newer).
- Relational integrity gate: PASS. Features touching Department-Employee relation define
  explicit FK rules and nullable employee assignment semantics.
- Sunset gate: PASS. Deprecated version cutoff behavior remains documented and enforces
  `410 Gone` using UTC as business clock.
- Pagination gate: PASS. List endpoints define defaults (`page=0`, `size=25`) and max (`100`) with validation tests.
- Workflow gate: PASS. Branch strategy, atomic commits, and PR traceability to spec/tasks
  are explicitly documented.

## Project Structure

### Documentation (this feature)

```text
specs/008-api-v3-departamentos/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
│   └── empleados-v3.openapi.yaml
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)
```text
src/main/java/com/dsw02/empleados/
├── config/
├── controller/
│   └── dto/
├── model/
├── repository/
└── service/

src/main/resources/
├── application.yml
└── db/migration/

src/test/java/com/dsw02/empleados/
├── contract/
└── integration/

docker/
└── docker-compose.yml
```

**Structure Decision**: Mantener el monolito por capas de Spring Boot existente.
Se agregaran componentes de `Departamento` en las capas controller/service/repository/model,
migraciones Flyway en `db/migration`, pruebas de integracion/contrato bajo la
estructura actual y un contrato OpenAPI v3 dedicado en la carpeta de feature.

## Phase 0: Research Output

- Ver `/specs/008-api-v3-departamentos/research.md`.

## Phase 1: Design Output

- Ver `/specs/008-api-v3-departamentos/data-model.md`.
- Ver `/specs/008-api-v3-departamentos/contracts/empleados-v3.openapi.yaml`.
- Ver `/specs/008-api-v3-departamentos/quickstart.md`.
- Decision de arquitectura: enforcement de integridad relacional con FK explicita,
  semantica HTTP `422` para referencia inexistente y `409` para conflicto de borrado.

## Post-Design Constitution Check

- Stack gate: PASS.
- Security gate: PASS.
- Data gate: PASS.
- Employee data gate: PASS.
- Contract gate: PASS.
- Quality gate: PASS.
- Versioning gate: PASS (declaracion oficial `v3`).
- Contract evolution gate: PASS.
- Relational integrity gate: PASS.
- Sunset gate: PASS (sin regresion para politicas vigentes).
- Pagination gate: PASS.
- Workflow gate: PASS.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | N/A | N/A |

## Delivery Notes (2026-03-18)

- Se implemento enforcement runtime de sunset para `v1` mediante filtro dedicado (`410 Gone` cuando aplique cutoff UTC).
- Se agregaron pruebas para:
  - migraciones Flyway `v10`/`v11`,
  - no-regresion de sunset `410` para rutas `v1`,
  - evaluacion UTC del cutoff en servicio de politicas,
  - limites de paginacion de `departamentos`.
- Se agregaron suites de performance (manuales) para objetivos P95 en `departamentos` y endpoint relacional.
- Se registro evidencia de medicion en `specs/008-api-v3-departamentos/evidence/performance/p95-baseline.md`.
- Validacion runtime en Docker Compose:
  - CRUD/relacional v3 funcional,
  - `DELETE` con dependencias retorna `409`,
  - objetivos P95 medidos en PASS para entorno local.
- Nota de entorno local: ejecuciones Maven con pruebas Testcontainers en host WSL pueden fallar por incompatibilidad de API Docker client/daemon; en ejecucion Docker Compose la API funciona correctamente.
