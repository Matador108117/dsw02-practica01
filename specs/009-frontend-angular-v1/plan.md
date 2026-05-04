# Implementation Plan: Frontend Angular V1

**Branch**: `009-frontend-angular-v1` | **Date**: 2026-03-19 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/009-frontend-angular-v1/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Incorporar frontend oficial Angular 22 LTS con login obligatorio, dashboard responsive
para Empleados/Departamentos, toggles CRUD, y despliegue Docker integrado al compose
existente. La autenticacion se implementa con JWT + refresh en `/api/v4/auth/*`
coexistiendo con Basic Auth del backend para rutas protegidas vigentes, con cookies
seguras (`HttpOnly`, `Secure`, `SameSite`) y proteccion CSRF explicita para refresh,
logout y operaciones CRUD.

## Technical Context

**Language/Version**: Java 17 + TypeScript (Angular 22 LTS)  
**Primary Dependencies**: Spring Boot 3, Spring Security, Spring Data JPA, springdoc-openapi, Angular 22 LTS, Cypress  
**Storage**: PostgreSQL (Docker-managed in local and CI)  
**Testing**: JUnit 5, Spring Boot Test, MockMvc/Testcontainers, Cypress E2E  
**Target Platform**: Linux server
**Project Type**: backend + official frontend web system  
**Performance Goals**:
- Login exitoso (P95) <= 3s en ambiente local dockerizado.
- Render inicial de tabla de entidad seleccionada (P95) <= 2.5s.
- Renovacion de sesion (`/api/v4/auth/refresh`) (P95) <= 500ms.
- Suite Cypress obligatoria <= 10 minutos en CI.  
**Constraints**: Mandatory HTTP Basic auth (`type=http`, `scheme=basic`) on API
methods using `correo_electronico` as username and transient `contrasena`
validated against persisted `contrasena_hash`, role-based access (`USER`
read-only, `ADMIN` CRUD), OpenAPI/Swagger required, Docker parity for DB,
API major versioning (`/api/v{major}`), mandatory major bump when public contract
changes or new public endpoints are introduced, paginated list endpoints,
explicit FK-backed relational integrity for `Departamento (1) -> (N) Empleados`
with nullable employee assignment, official frontend stack (Angular 22 LTS +
TypeScript + nvm), frontend Docker + existing Compose integration, Cypress E2E
gate for login success/failure + employee/department rendering + CRUD, frontend
versioning independent from backend, feature-branch + PR workflow  
**Scale/Scope**:
- 1 SPA oficial (Angular) con autenticacion y dashboard CRUD.
- Consumo de endpoints existentes de `empleados` y `departamentos` en `/api/v3`.
- Nuevos endpoints de auth en v4: login + refresh + logout.
- Usuarios simultaneos esperados (entorno academico): bajo a medio (<100 concurrentes).

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- Stack gate: PASS. Backend mantiene Spring Boot 3 + Java 17; frontend oficial usa Angular 22 LTS + TypeScript + nvm.
- Security gate: Protected endpoints include mandatory HTTP Basic auth (`type=http`,
  `scheme=basic`) with `correo_electronico` username mapping, transient password
  verification against persisted hash, plus role policy (`USER` read-only,
  `ADMIN` CRUD) design and test strategy.
  Status: PASS with explicit coexistence strategy Basic + JWT for frontend session flows.
- Data gate: PASS. PostgreSQL schema impact for refresh/session revocation is documented in design artifacts.
- Employee data gate: Any feature touching `empleado` persistence documents and
  enforces required `correo_electronico` and `contrasena_hash` attributes, while
  keeping `contrasena` as input-only and not persisted in plaintext.
  Status: PASS. No plaintext persistence introduced.
- Contract gate: PASS. OpenAPI contract for `/api/v4/auth/login`, `/api/v4/auth/refresh`
  y `/api/v4/auth/logout` added in `contracts/`.
- Quality gate: PASS. Integration and Cypress E2E suites planned as blocking gates.
- Frontend platform gate: Official frontend uses Angular 22 LTS + TypeScript,
  Node version management through nvm, and integrates with the existing Docker
  Compose topology.
  Status: PASS.
- Frontend integration gate: Frontend consumes only official API endpoints,
  delegates authentication to API flows, and does not duplicate domain logic.
  Status: PASS.
- Frontend E2E gate: Cypress scenarios for login success, login failure,
  employee rendering, department rendering, and CRUD are planned and blocking.
  Status: PASS.
- Versioning gate: API path version impact is documented (`/api/v{major}`) and
  breaking changes include migration notes.
  Status: PASS. Feature usa v4 para auth (`/api/v4/auth/*`) y mantiene v3 para
  endpoints de dominio existentes.
- Frontend version gate: Frontend release version impact is documented separately
  from backend API versioning.
  Status: PASS.
- Contract evolution gate: Public contract expansion or new endpoints explicitly
  trigger major version increment (for Department-domain rollout: `v3` or newer).
  Status: PASS. New public auth endpoints are versioned in v4.
- Relational integrity gate: Features touching Department-Employee relation define
  explicit FK rules and nullable employee assignment semantics.
  Status: PASS. No relational changes required.
- Sunset gate: Deprecated version cutoff behavior is documented and enforces
  `410 Gone` using UTC as business clock.
  Status: PASS, unchanged by this feature.
- Pagination gate: PASS. Existing paginated list behavior is consumed by frontend and validated in E2E.
- Workflow gate: Branch strategy, atomic commits, and PR traceability to spec/tasks
  are explicitly documented.
  Status: PASS.

## Project Structure

### Documentation (this feature)

```text
specs/009-frontend-angular-v1/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command)
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

frontend/
├── src/
├── cypress/
└── package.json

docker/
└── docker-compose.yml
```

**Structure Decision**:
- Existing backend remains in `src/main/java/com/dsw02/empleados/*` with current v3 controllers.
- New frontend app will be created under `frontend/` (Angular workspace + Cypress).
- Auth contract and planning artifacts remain under `specs/009-frontend-angular-v1/`.
- Existing compose at `docker/docker-compose.yml` is extended with `frontend` service
  pointing to `../frontend` build context.

## Endpoint Exposure Policy

- `POST /api/v4/auth/login`: public bootstrap endpoint, JSON-only credentials, rate-limited.
- `POST /api/v4/auth/refresh`: public-controlled endpoint, requires secure cookie and CSRF token.
- `POST /api/v4/auth/logout`: authenticated endpoint for explicit session revocation.
- `/api/v3/empleados/**` and `/api/v3/departamentos/**`: protected endpoints with role policy.

## Post-Design Constitution Check

- Stack and platform gates: PASS after design artifacts (`research.md`, `data-model.md`,
  `quickstart.md`, `contracts/auth-v4.openapi.yaml`).
- Security/auth gates: PASS with explicit JWT+refresh design for frontend and preserved
  Basic Auth compatibility for existing protected APIs.
- Contract/versioning gates: PASS. Auth endpoints are under `/api/v4`; domain endpoints remain in `/api/v3`.
- Testing/quality gates: PASS. Cypress suite is defined as non-optional build gate.
- Docker/integration gates: PASS. Frontend Docker + compose integration explicitly planned.
- Governance follow-up: Constitutional coexistence update is handled as separate
  governance change outside feature task execution.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| Public login endpoint exception to global Basic-only policy | Frontend must bootstrap auth without pre-existing Basic header | Forcing Basic on login would require storing plaintext credentials and degrade security posture |
