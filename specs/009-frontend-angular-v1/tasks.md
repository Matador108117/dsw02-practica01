# Tasks: Frontend Angular V1

**Input**: Design documents from `/specs/009-frontend-angular-v1/`
**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/, quickstart.md

**Tests**: Tests are mandatory for this feature (backend integration/contract + Cypress E2E).
**Organization**: Tasks are grouped by user story to preserve independent implementation and validation.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Bootstrap frontend project scaffolding and runtime wiring.

- [X] T001 Create Angular 22 workspace skeleton in frontend/angular.json and frontend/package.json
- [X] T002 Configure Node version management in frontend/.nvmrc
- [X] T003 [P] Configure TypeScript strict mode and path aliases in frontend/tsconfig.json
- [X] T004 [P] Create production container build for frontend in frontend/Dockerfile
- [X] T005 [P] Add static server configuration for SPA routes in frontend/nginx.conf
- [X] T006 Integrate frontend service into compose stack in docker/docker-compose.yml

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core backend/frontend auth and platform capabilities required by all stories.

**⚠️ CRITICAL**: No user story work begins until this phase is complete.

- [X] T007 Create refresh-session persistence model in src/main/java/com/dsw02/empleados/model/RefreshTokenSession.java
- [X] T008 [P] Create refresh-session repository in src/main/java/com/dsw02/empleados/repository/RefreshTokenSessionRepository.java
- [X] T009 Add refresh-session Flyway migration in src/main/resources/db/migration/V12__create_refresh_token_session_table.sql
- [X] T010 [P] Implement JWT utility service in src/main/java/com/dsw02/empleados/service/JwtService.java
- [X] T011 Implement auth application service in src/main/java/com/dsw02/empleados/service/AuthServiceImpl.java
- [X] T012 [P] Define auth request/response DTOs in src/main/java/com/dsw02/empleados/controller/dto/AuthDtos.java
- [X] T013 Update security config for Basic+JWT coexistence and auth route policy in src/main/java/com/dsw02/empleados/config/SecurityConfig.java
- [X] T014 Implement CSRF token issuance/validation for sensitive endpoints in src/main/java/com/dsw02/empleados/config/SecurityConfig.java
- [X] T015 Record governance evidence link for approved Basic+JWT coexistence amendment in specs/009-frontend-angular-v1/quickstart.md [Req: FR-025]

Note: Constitutional coexistence amendment is handled as separate governance change;
this feature only records evidence linkage.

**Checkpoint**: Foundation complete, user stories can start.

---

## Phase 3: User Story 1 - Autenticacion y Acceso Seguro (Priority: P1) 🎯 MVP

**Goal**: Deliver mandatory login, protected navigation, JWT session/refresh, and secure cookie behavior.

**Independent Test**: Accessing protected routes without auth redirects to login, valid login returns session, invalid login returns 401, refresh renews token, logout invalidates session.

### Tests for User Story 1

- [X] T016 [P] [US1] Add auth contract tests for `/api/v4/auth/login`, `/api/v4/auth/refresh`, and `/api/v4/auth/logout`, asserting login payload schema (`status`, `role`) and absence of `token`/`refreshToken` in body for both `200` and `401` responses, in src/test/java/com/dsw02/empleados/contract/AuthV4ContractIT.java [Req: FR-026]
- [X] T017 [P] [US1] Add integration tests for login/refresh/logout + cookie flags in src/test/java/com/dsw02/empleados/integration/AuthIntegrationIT.java
- [X] T018 [P] [US1] Add Cypress login success/failure and protected-route redirect tests in frontend/cypress/e2e/auth/login.cy.ts
- [X] T053 [P] [US1] Add Cypress session rehydration tests for browser reopen/restore and refresh-token expiry boundary in frontend/cypress/e2e/auth/session-rehydration.cy.ts [Req: FR-029]

### Implementation for User Story 1

- [X] T019 [P] [US1] Implement auth REST controller in src/main/java/com/dsw02/empleados/controller/AuthController.java
- [X] T020 [US1] Implement login, refresh, and logout flow for `/api/v4/auth/*` in src/main/java/com/dsw02/empleados/service/AuthServiceImpl.java
- [X] T021 [US1] Implement secure auth cookie management in src/main/java/com/dsw02/empleados/service/AuthCookieService.java
- [X] T022 [P] [US1] Implement frontend auth API client in frontend/src/app/core/api/auth-api.service.ts
- [X] T023 [US1] Implement auth state store and route guard in frontend/src/app/core/auth/auth.store.ts and frontend/src/app/core/auth/auth.guard.ts
- [X] T024 [US1] Implement login page, required field validation, and redirect flow in frontend/src/app/features/auth/login-page.component.ts

**Checkpoint**: User can authenticate and navigate securely with session handling.

---

## Phase 4: User Story 2 - Dashboard de Entidades (Priority: P2)

**Goal**: Deliver responsive dashboard layout, sidebar/entity selection, and dynamic rendering of empleados/departamentos.

**Independent Test**: After login, dashboard loads with grid layout, entity switching works, tables render correctly, and footer version is visible.

### Tests for User Story 2

- [X] T025 [P] [US2] Add Cypress entity rendering tests for empleados and departamentos in frontend/cypress/e2e/dashboard/render-entities.cy.ts
- [X] T026 [P] [US2] Add component tests for sidebar search/selection in frontend/src/app/features/dashboard/sidebar/sidebar.component.spec.ts
- [X] T027 [P] [US2] Add integration test coverage for `/api/v3/departamentos/{id}/empleados` rendering contract in src/test/java/com/dsw02/empleados/integration/DepartamentoEmpleadosEndpointIT.java [Req: FR-016]
- [X] T028 [P] [US2] Add integration/contract tests for pagination defaults and max limits on consumed list endpoints in src/test/java/com/dsw02/empleados/contract/PaginationDefaultsContractIT.java [Req: BC-009]
- [X] T059 [P] [US2] Add Cypress responsive assertions for 375x667 and 1366x768, validating sidebar navigation flow, logout visibility, and `body.scrollWidth == viewportWidth` in frontend/cypress/e2e/dashboard/responsive-layout.cy.ts [Req: SC-004]

### Implementation for User Story 2

- [X] T029 [P] [US2] Build dashboard shell grid layout in frontend/src/app/features/dashboard/dashboard-shell.component.ts and frontend/src/app/features/dashboard/dashboard-shell.component.scss
- [X] T030 [US2] Implement header, subtitle, and footer version components in frontend/src/app/features/dashboard/header/header.component.ts and frontend/src/app/features/dashboard/footer/footer.component.ts
- [X] T031 [US2] Implement sidebar with API-driven entity list, search, and logout action in frontend/src/app/features/dashboard/sidebar/sidebar.component.ts
- [X] T032 [US2] Implement responsive entity table component in frontend/src/app/features/dashboard/entity-table/entity-table.component.ts
- [X] T033 [P] [US2] Implement empleados and departamentos API clients in frontend/src/app/core/api/empleados-api.service.ts and frontend/src/app/core/api/departamentos-api.service.ts
- [X] T034 [US2] Implement mobile sidebar collapse behavior in frontend/src/app/features/dashboard/dashboard-shell.component.scss

**Checkpoint**: Dashboard UI is functional and independently testable for read flows.

---

## Phase 5: User Story 3 - Operaciones CRUD con Toggles (Priority: P3)

**Goal**: Deliver CRUD toggle cards for empleados/departamentos and departamento empleados drilldown.

**Independent Test**: User can create/edit/delete from toggle cards and execute "ver empleados del departamento" flow with expected table updates.

### Tests for User Story 3

- [X] T035 [P] [US3] Add Cypress CRUD toggle tests for empleados/departamentos in frontend/cypress/e2e/dashboard/crud-toggles.cy.ts
- [X] T036 [P] [US3] Add Cypress test for departamento empleados toggle using `/api/v3/departamentos/{id}/empleados` in frontend/cypress/e2e/dashboard/departamento-empleados.cy.ts
- [X] T037 [P] [US3] Add integration tests for CSRF enforcement on auth-sensitive CRUD requests in src/test/java/com/dsw02/empleados/integration/AuthCsrfCrudIT.java

### Implementation for User Story 3

- [X] T038 [P] [US3] Create reusable toggle card base component in frontend/src/app/features/dashboard/actions/action-toggle-card.component.ts
- [X] T039 [US3] Implement add/edit/delete toggle card components in frontend/src/app/features/dashboard/actions/add-card.component.ts and frontend/src/app/features/dashboard/actions/edit-card.component.ts and frontend/src/app/features/dashboard/actions/delete-card.component.ts
- [X] T040 [US3] Implement departamento empleados toggle card in frontend/src/app/features/dashboard/actions/view-dept-empleados-card.component.ts
- [X] T041 [US3] Wire CRUD action orchestration and role-aware behavior in frontend/src/app/features/dashboard/dashboard.facade.ts
- [X] T042 [US3] Implement frontend CSRF interceptor for sensitive calls in frontend/src/app/core/http/csrf.interceptor.ts
- [X] T043 [US3] Extend backend auth/error mapping for auth and CSRF failures in src/main/java/com/dsw02/empleados/controller/GlobalExceptionHandler.java

**Checkpoint**: Full CRUD toggle behavior is independently testable.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Complete release hardening, documentation, and full validation.

- [X] T044 [P] Update OpenAPI auth documentation and examples in src/main/java/com/dsw02/empleados/config/OpenApiConfig.java and specs/009-frontend-angular-v1/contracts/auth-v4.openapi.yaml [Req: FR-006, FR-026]
- [X] T045 [P] Add CI quality gates for Maven + Cypress in .github/workflows/ci.yml [Req: FR-022]
- [X] T046 [P] Finalize frontend compose/runtime health settings in docker/docker-compose.yml and frontend/Dockerfile [Req: FR-020]
- [X] T047 [P] Add integration-test evidence matrix for auth, role authorization, DB, and API contract suites in specs/009-frontend-angular-v1/quickstart.md and .github/workflows/ci.yml [Req: BC-007]
- [X] T048 Validate quickstart end-to-end steps and expected outputs in specs/009-frontend-angular-v1/quickstart.md [Req: FR-021]
- [X] T049 Document frontend release/versioning policy in docs/frontend-release.md [Req: FR-023, FC-012]
- [X] T050 [P] Add login performance benchmark and CI threshold gate (P95 <= 3s) in src/test/java/com/dsw02/empleados/performance/AuthLoginPerformanceIT.java and .github/workflows/ci.yml [Req: SC-002]
- [X] T051 [P] Add architecture rule and lint/test to prevent frontend business logic duplication in frontend/eslint.config.js and frontend/src/app/core/architecture/logic-boundary.spec.ts [Req: FC-005]
- [X] T052 Add implementation traceability checklist (tasks -> commits -> PR evidence) in specs/009-frontend-angular-v1/quickstart.md [Req: BC-010]
- [X] T054 [P] Add dashboard first-render performance benchmark and CI gate (P95 <= 2.5s) in frontend/cypress/e2e/perf/dashboard-render.cy.ts and .github/workflows/ci.yml [Req: SC-006]
- [X] T055 [P] Add auth refresh performance benchmark and CI gate (P95 <= 500ms) in src/test/java/com/dsw02/empleados/performance/AuthRefreshPerformanceIT.java and .github/workflows/ci.yml [Req: SC-007]
- [X] T056 [P] Add integration test coverage for required `correo_electronico` and `contrasena_hash` persistence constraints plus no-plaintext password persistence in src/test/java/com/dsw02/empleados/integration/EmpleadoPersistenceConstraintsIT.java [Req: BC-011, BC-012]
- [X] T057 [P] Add contract test for API sunset behavior returning `410 Gone` with UTC cutoff evaluation in src/test/java/com/dsw02/empleados/contract/ApiSunsetBehaviorContractIT.java [Req: BC-013]
- [X] T058 [P] Add relational integrity tests for Departamento->Empleado cardinality, nullable assignment, and explicit FK enforcement in src/test/java/com/dsw02/empleados/integration/DepartamentoEmpleadoIntegrityIT.java [Req: BC-014, BC-015]
- [X] T060 [P] Configure Cypress CI artifact publication (videos, screenshots, JUnit/JSON) for both passed and failed runs in .github/workflows/ci.yml [Req: FC-010]
- [X] T061 [P] Add CI enforcement step to fail pipeline when mandatory Cypress evidence artifacts are missing in .github/workflows/ci.yml [Req: FC-011]
- [X] T062 Add release verification gate for frontend footer version visibility and deployment artifact traceability in docs/frontend-release.md and .github/workflows/ci.yml [Req: SC-005]
- [X] T063 [P] Validate USER read-only and ADMIN CRUD role matrix in existing integration suite src/test/java/com/dsw02/empleados/integration/SecurityCrudIntegrationIT.java and link evidence in specs/009-frontend-angular-v1/quickstart.md [Req: BC-003]
- [X] T064 [P] Link explicit Basic-auth evidence for username=`correo_electronico` mapping and hash-comparison validation using existing suites src/test/java/com/dsw02/empleados/integration/BasicAuthUsernameMappingIntegrationIT.java and src/test/java/com/dsw02/empleados/integration/BasicAuthHashValidationIntegrationIT.java in specs/009-frontend-angular-v1/quickstart.md [Req: BC-002a, BC-002b]
- [X] T065 [P] Add visual accessibility verification for contrast/transition policy in frontend/cypress/e2e/dashboard/visual-accessibility.cy.ts and include CI evidence linkage in .github/workflows/ci.yml [Req: FR-018]

---

## Dependencies & Execution Order

### Phase Dependencies

- Phase 1 (Setup): no dependencies.
- Phase 2 (Foundational): depends on Phase 1 and blocks all user stories.
- Phase 3 (US1): depends on Phase 2.
- Phase 4 (US2): depends on Phase 2 and uses auth baseline from US1.
- Phase 5 (US3): depends on Phase 2 and integrates with US2 dashboard components.
- Phase 6 (Polish): depends on completion of selected user stories.

### User Story Dependencies

- **US1 (P1)**: no dependency on other stories after foundational phase.
- **US2 (P2)**: is independently verifiable once US1 authentication baseline is available.
- **US3 (P3)**: is independently verifiable once US1+US2 baseline UI/auth layers are available.

### Parallel Opportunities

- Setup tasks `T003`, `T004`, `T005` can run in parallel.
- Foundational tasks `T008`, `T010`, `T012` can run in parallel after `T007`.
- US1 tests `T016`-`T018` and `T053` can run in parallel.
- US2 tests `T025`-`T028` and `T059` can run in parallel.
- US3 tests `T035`-`T037` can run in parallel.
- Polish tasks `T044`, `T045`, `T046`, `T047`, `T050`, `T051`, `T054`, `T055`, `T056`, `T057`, `T058`, `T060`, `T061`, `T063`, `T064`, `T065` can run in parallel.

---

## Parallel Execution Examples

### User Story 1

```bash
# Parallel test authoring
T016 Auth contract tests
T017 Auth integration tests
T018 Cypress auth E2E
T053 Cypress session rehydration and expiry

# Parallel implementation chunks
T019 AuthController
T022 Frontend auth API client
```

### User Story 2

```bash
# Parallel test authoring
T025 Cypress render tests
T026 Sidebar component tests
T027 Departamento empleados integration test
T028 Pagination defaults/max contract tests
T059 Cypress responsive viewport assertions

# Parallel implementation chunks
T029 Dashboard shell layout
T033 API clients for empleados/departamentos
```

### User Story 3

```bash
# Parallel test authoring
T035 Cypress CRUD toggles
T036 Cypress departamento empleados toggle
T037 CSRF CRUD integration tests

# Parallel implementation chunks
T038 ActionToggleCard base
T042 CSRF interceptor
```

---

## Implementation Strategy

### MVP First (US1 only)

1. Complete Phase 1 + Phase 2.
2. Deliver Phase 3 (US1) end-to-end.
3. Validate login, refresh, logout, and route guard flows.
4. Demo secure authentication baseline.

### Incremental Delivery

1. Add US2 dashboard read experience after US1.
2. Add US3 CRUD toggles and departamento empleados drilldown.
3. Close with Phase 6 polish and release documentation.

### Team Strategy

1. Team A: backend auth/JWT + security integration (US1-heavy).
2. Team B: frontend shell/navigation/table rendering (US2-heavy).
3. Team C: toggle cards + E2E automation (US3-heavy).

---

## Validation Checklist

- All tasks use required format: checkbox + TaskID + optional [P] + optional [US#] + explicit file path.
- Each user story phase is independently testable.
- Mandatory E2E coverage is explicitly included.
- API contract, performance gate, and architecture-boundary tasks are included.
