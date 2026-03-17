# Tasks: CRUD de Empleados

**Input**: Design documents from `/specs/003-crud-empleados/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Se incluyen tareas de pruebas porque la especificación exige cobertura de integración para autenticación, persistencia y contrato de API (BC-006).

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Inicialización del proyecto Spring Boot 3 + Java 17 y base de documentación técnica.

- [ ] T001 Create Maven Spring Boot project scaffold in pom.xml
- [ ] T002 Configure base package and application bootstrap in src/main/java/com/dsw02/empleados/EmpleadosApplication.java
- [ ] T003 [P] Add environment defaults for PostgreSQL and Swagger in src/main/resources/application.yml
- [ ] T004 [P] Add Spring Boot starter dependencies (web, security, data-jpa, validation, springdoc) in pom.xml
- [ ] T005 [P] Add test dependencies (spring-boot-starter-test, spring-security-test, testcontainers-postgresql) in pom.xml

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Infraestructura base obligatoria antes de cualquier historia de usuario.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [ ] T006 Configure PostgreSQL container runtime for local/CI in docker/docker-compose.yml
- [ ] T007 Create initial schema migration for table `empleado` in src/main/resources/db/migration/V1__create_empleado_table.sql
- [ ] T008 [P] Implement HTTP Basic Auth security configuration in src/main/java/com/dsw02/empleados/config/SecurityConfig.java
- [ ] T009 [P] Define OpenAPI security scheme and API metadata config in src/main/java/com/dsw02/empleados/config/OpenApiConfig.java
- [ ] T010 [P] Implement global exception handling for validation, not-found, and duplicate-key errors in src/main/java/com/dsw02/empleados/controller/GlobalExceptionHandler.java
- [ ] T011 Create JPA entity for Empleado with field constraints in src/main/java/com/dsw02/empleados/model/Empleado.java
- [ ] T012 [P] Create repository interface for Empleado access by clave in src/main/java/com/dsw02/empleados/repository/EmpleadoRepository.java
- [ ] T013 [P] Create DTOs for create/update/response payloads in src/main/java/com/dsw02/empleados/controller/dto/EmpleadoDtos.java
- [ ] T014 Add service contract for CRUD operations in src/main/java/com/dsw02/empleados/service/EmpleadoService.java
- [ ] T015 Add structured audit logging helper for alta/actualización/eliminación events in src/main/java/com/dsw02/empleados/config/AuditLogHelper.java

**Checkpoint**: Foundation ready - user story implementation can now begin.

---

## Phase 3: User Story 1 - Registrar empleado (Priority: P1) 🎯 MVP

**Goal**: Permitir alta de empleado con `clave` única entera y campos obligatorios (`nombre`, `direccion`, `telefono`) de máximo 100.

**Independent Test**: Crear un empleado válido autenticado y verificar persistencia; rechazar duplicado, campos vacíos y longitudes > 100.

### Tests for User Story 1

- [ ] T016 [P] [US1] Add contract test for POST /api/empleados success and validation errors in src/test/java/contract/EmpleadoCreateContractIT.java
- [ ] T017 [P] [US1] Add integration test for POST /api/empleados with Basic Auth and PostgreSQL persistence in src/test/java/integration/EmpleadoCreateIntegrationIT.java

### Implementation for User Story 1

- [ ] T018 [US1] Implement create operation with duplicate-key validation in src/main/java/com/dsw02/empleados/service/EmpleadoServiceImpl.java
- [ ] T019 [US1] Implement POST /api/empleados endpoint with request validation in src/main/java/com/dsw02/empleados/controller/EmpleadoController.java
- [ ] T020 [US1] Add audit log emission for successful employee creation in src/main/java/com/dsw02/empleados/service/EmpleadoServiceImpl.java
- [ ] T021 [US1] Document POST /api/empleados operation annotations for Swagger in src/main/java/com/dsw02/empleados/controller/EmpleadoController.java

**Checkpoint**: User Story 1 funciona y se valida de forma independiente.

---

## Phase 4: User Story 2 - Consultar empleados (Priority: P2)

**Goal**: Permitir consulta de listado y detalle por `clave` con control de no encontrado.

**Independent Test**: Listar empleados existentes y consultar detalle por clave; retorno `404` para clave inexistente.

### Tests for User Story 2

- [ ] T022 [P] [US2] Add contract tests for GET /api/empleados and GET /api/empleados/{clave} in src/test/java/contract/EmpleadoReadContractIT.java
- [ ] T023 [P] [US2] Add integration tests for authenticated read flows and 404 behavior in src/test/java/integration/EmpleadoReadIntegrationIT.java

### Implementation for User Story 2

- [ ] T024 [US2] Implement list and get-by-clave service methods in src/main/java/com/dsw02/empleados/service/EmpleadoServiceImpl.java
- [ ] T025 [US2] Implement GET /api/empleados and GET /api/empleados/{clave} endpoints in src/main/java/com/dsw02/empleados/controller/EmpleadoController.java
- [ ] T026 [US2] Add not-found domain exception and mapping for consulta por clave in src/main/java/com/dsw02/empleados/service/EmpleadoNotFoundException.java
- [ ] T027 [US2] Document read endpoints and error responses in Swagger annotations in src/main/java/com/dsw02/empleados/controller/EmpleadoController.java

**Checkpoint**: User Stories 1 y 2 funcionan y se validan independientemente.

---

## Phase 5: User Story 3 - Actualizar y eliminar empleado (Priority: P3)

**Goal**: Permitir actualizar campos de texto y eliminar empleados por `clave` con validaciones y no encontrado.

**Independent Test**: Actualizar un empleado existente y eliminarlo; verificar rechazo de >100 caracteres y `404` para clave inexistente.

### Tests for User Story 3

- [ ] T028 [P] [US3] Add contract tests for PUT/DELETE /api/empleados/{clave} in src/test/java/contract/EmpleadoUpdateDeleteContractIT.java
- [ ] T029 [P] [US3] Add integration tests for update/delete flows including 404 and validation in src/test/java/integration/EmpleadoUpdateDeleteIntegrationIT.java

### Implementation for User Story 3

- [ ] T030 [US3] Implement update and delete service methods with existence checks in src/main/java/com/dsw02/empleados/service/EmpleadoServiceImpl.java
- [ ] T031 [US3] Implement PUT and DELETE endpoints in src/main/java/com/dsw02/empleados/controller/EmpleadoController.java
- [ ] T032 [US3] Add audit log emission for update and delete events in src/main/java/com/dsw02/empleados/service/EmpleadoServiceImpl.java
- [ ] T033 [US3] Document update/delete endpoints and response codes in Swagger annotations in src/main/java/com/dsw02/empleados/controller/EmpleadoController.java

**Checkpoint**: Todas las historias de usuario CRUD están completas y verificables.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Cierre técnico, evidencia de calidad y alineación final de contrato.

- [ ] T034 [P] Align implemented API annotations with contract in specs/003-crud-empleados/contracts/empleados.openapi.yaml
- [ ] T035 Add integration test for unauthorized access across CRUD endpoints in src/test/java/integration/SecurityCrudIntegrationIT.java
- [ ] T036 [P] Add quickstart verification notes and execution commands in specs/003-crud-empleados/quickstart.md
- [ ] T037 Run full test suite and document outcomes in specs/003-crud-empleados/checklists/requirements.md

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies.
- **Phase 2 (Foundational)**: Depends on Phase 1; blocks all user stories.
- **Phase 3+ (User Stories)**: Depend on Phase 2 completion.
- **Phase 6 (Polish)**: Depends on all target user stories complete.

### User Story Dependencies

- **US1 (P1)**: Starts after Foundational; no dependency on other stories.
- **US2 (P2)**: Starts after Foundational; can be validated independently but reuses base service/controller.
- **US3 (P3)**: Starts after Foundational; can be validated independently but reuses base service/controller.

### Within Each User Story

- Tests first (contract + integration), then implementation.
- Service logic before endpoint wiring where possible.
- Swagger docs updated in same story phase.

## Parallel Opportunities

- **Setup**: T003, T004, T005 can run in parallel after T001.
- **Foundational**: T008, T009, T010, T012, T013 can run in parallel after T006/T007.
- **US1**: T016 and T017 parallel; then implementation sequence T018–T021.
- **US2**: T022 and T023 parallel; then T024–T027.
- **US3**: T028 and T029 parallel; then T030–T033.
- **Polish**: T034 and T036 parallel; T035 before final T037.

---

## Parallel Example: User Story 1

```bash
# Ejecutar en paralelo (si hay capacidad de equipo):
Task: "T016 [US1] Contract test for POST /api/empleados in src/test/java/contract/EmpleadoCreateContractIT.java"
Task: "T017 [US1] Integration test for POST /api/empleados in src/test/java/integration/EmpleadoCreateIntegrationIT.java"
```

## Parallel Example: User Story 2

```bash
Task: "T022 [US2] Contract tests for GET endpoints in src/test/java/contract/EmpleadoReadContractIT.java"
Task: "T023 [US2] Integration tests for GET endpoints in src/test/java/integration/EmpleadoReadIntegrationIT.java"
```

## Parallel Example: User Story 3

```bash
Task: "T028 [US3] Contract tests for PUT/DELETE endpoints in src/test/java/contract/EmpleadoUpdateDeleteContractIT.java"
Task: "T029 [US3] Integration tests for PUT/DELETE endpoints in src/test/java/integration/EmpleadoUpdateDeleteIntegrationIT.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 and Phase 2.
2. Complete Phase 3 (US1).
3. Validate independently with T016-T017 + acceptance scenarios.
4. Demo/deploy MVP.

### Incremental Delivery

1. Foundation complete (Phases 1-2).
2. Deliver US1 (create) as MVP.
3. Deliver US2 (read) without regressing US1.
4. Deliver US3 (update/delete) and finalize polish.

### Suggested MVP Scope

- **MVP recomendado**: Phase 1 + Phase 2 + Phase 3 (US1).
- Entrega mínima de valor: alta de empleado con seguridad, validación, persistencia y contrato documentado.
