# Tasks: CRUD de Empleados con Clave Compuesta

**Input**: Design documents from `/specs/001-actualizar-clave-compuesta/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Se incluyen tareas de pruebas porque la especificación exige integración para autenticación, persistencia y contrato (BC-006), además de validar concurrencia de claves (SC-002).

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Inicializar proyecto Spring Boot 3 + Java 17 y base de configuración.

- [X] T001 Create Spring Boot project scaffold and packaging in pom.xml
- [X] T002 Create application bootstrap class in src/main/java/com/dsw02/empleados/EmpleadosApplication.java
- [X] T003 [P] Configure default application properties for PostgreSQL and Swagger in src/main/resources/application.yml
- [X] T004 [P] Add core dependencies (web, security, data-jpa, validation, springdoc-openapi) in pom.xml
- [X] T005 [P] Add testing dependencies (spring-boot-starter-test, spring-security-test, testcontainers-postgresql) in pom.xml

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Infraestructura técnica compartida que bloquea toda historia de usuario hasta completarse.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [X] T006 Configure Docker PostgreSQL runtime for local and CI in docker/docker-compose.yml
- [X] T007 Create Flyway migration for empleado table with composite PK in src/main/resources/db/migration/V1__create_empleado_table.sql
- [X] T008 Create Flyway migration for consecutivo sequence and defaults in src/main/resources/db/migration/V2__create_empleado_sequence.sql
- [X] T009 [P] Implement HTTP Basic Auth security rules in src/main/java/com/dsw02/empleados/config/SecurityConfig.java
- [X] T010 [P] Configure OpenAPI metadata and basicAuth security scheme in src/main/java/com/dsw02/empleados/config/OpenApiConfig.java
- [X] T011 [P] Implement global exception handling (validation, bad-format clave, not found) in src/main/java/com/dsw02/empleados/controller/GlobalExceptionHandler.java
- [X] T012 Create composite key class for empleado identity in src/main/java/com/dsw02/empleados/model/ClaveEmpleadoId.java
- [X] T013 Create Empleado JPA entity with composite PK and constraints in src/main/java/com/dsw02/empleados/model/Empleado.java
- [X] T014 [P] Create repository with clave lookup and sequence support in src/main/java/com/dsw02/empleados/repository/EmpleadoRepository.java
- [X] T015 [P] Create DTO models for create/update/response payloads in src/main/java/com/dsw02/empleados/controller/dto/EmpleadoDtos.java
- [X] T016 Define service interface for CRUD with composite clave in src/main/java/com/dsw02/empleados/service/EmpleadoService.java
- [X] T017 Add structured logging configuration for critical flows and startup/runtime failures in src/main/resources/application.yml

**Checkpoint**: Foundation complete; user stories can start.

---

## Phase 3: User Story 1 - Registrar empleado con clave generada (Priority: P1) 🎯 MVP

**Goal**: Registrar empleados sin clave manual, generando `EMP-` + consecutivo numérico único.

**Independent Test**: Crear empleados válidos y verificar clave autogenerada con formato correcto, validaciones de campos y ausencia de colisiones en concurrencia.

### Tests for User Story 1

- [X] T018 [P] [US1] Add contract tests for POST /api/empleados success and 400 responses in src/test/java/contract/EmpleadoCreateContractIT.java
- [X] T019 [P] [US1] Add integration tests for create with Basic Auth and persistence in src/test/java/integration/EmpleadoCreateIntegrationIT.java
- [X] T020 [P] [US1] Add concurrent create integration test for unique consecutivo generation in src/test/java/integration/EmpleadoCreateConcurrencyIT.java

### Implementation for User Story 1

- [X] T021 [US1] Implement create service logic with transactional sequence-based clave generation in src/main/java/com/dsw02/empleados/service/EmpleadoServiceImpl.java
- [X] T022 [US1] Implement clave formatter helper (`EMP-` + zero padding) in src/main/java/com/dsw02/empleados/service/ClaveEmpleadoFormatter.java
- [X] T023 [US1] Implement POST /api/empleados endpoint rejecting manual clave input in src/main/java/com/dsw02/empleados/controller/EmpleadoController.java
- [X] T024 [US1] Emit structured audit event for ALTA operation in src/main/java/com/dsw02/empleados/service/EmpleadoServiceImpl.java
- [X] T025 [US1] Add OpenAPI annotations for create endpoint and response model in src/main/java/com/dsw02/empleados/controller/EmpleadoController.java

**Checkpoint**: US1 funciona de forma independiente y constituye el MVP.

---

## Phase 4: User Story 2 - Consultar empleados por clave compuesta (Priority: P2)

**Goal**: Consultar listado y detalle usando clave compuesta (`EMP-000001`).

**Independent Test**: Listar empleados y consultar detalle por clave compuesta existente/no existente.

### Tests for User Story 2

- [X] T026 [P] [US2] Add contract tests for GET /api/empleados and GET /api/empleados/{clave} in src/test/java/contract/EmpleadoReadContractIT.java
- [X] T027 [P] [US2] Add integration tests for read flows and 404 behavior in src/test/java/integration/EmpleadoReadIntegrationIT.java

### Implementation for User Story 2

- [X] T028 [US2] Implement service methods for list and get-by-composite-clave in src/main/java/com/dsw02/empleados/service/EmpleadoServiceImpl.java
- [X] T029 [US2] Implement parser/validator for clave path format `EMP-` + dígitos in src/main/java/com/dsw02/empleados/service/ClaveEmpleadoParser.java
- [X] T030 [US2] Implement GET endpoints for list and detail in src/main/java/com/dsw02/empleados/controller/EmpleadoController.java
- [X] T031 [US2] Add not-found exception for composite clave lookups in src/main/java/com/dsw02/empleados/service/EmpleadoNotFoundException.java
- [X] T032 [US2] Add OpenAPI annotations for read endpoints and 404 response in src/main/java/com/dsw02/empleados/controller/EmpleadoController.java

**Checkpoint**: US2 funcional y verificable sin depender de US3.

---

## Phase 5: User Story 3 - Actualizar y eliminar con clave compuesta (Priority: P3)

**Goal**: Actualizar y eliminar empleados por clave compuesta manteniendo clave inmutable.

**Independent Test**: Actualizar y eliminar por clave existente; validar 404 en inexistente y rechazo de cambios de clave.

### Tests for User Story 3

- [X] T033 [P] [US3] Add contract tests for PUT/DELETE /api/empleados/{clave} in src/test/java/contract/EmpleadoUpdateDeleteContractIT.java
- [X] T034 [P] [US3] Add integration tests for update/delete and immutable-clave behavior in src/test/java/integration/EmpleadoUpdateDeleteIntegrationIT.java

### Implementation for User Story 3

- [X] T035 [US3] Implement update/delete service methods with existence checks in src/main/java/com/dsw02/empleados/service/EmpleadoServiceImpl.java
- [X] T036 [US3] Enforce clave immutability in update request handling in src/main/java/com/dsw02/empleados/service/EmpleadoServiceImpl.java
- [X] T037 [US3] Implement PUT and DELETE endpoints by composite clave in src/main/java/com/dsw02/empleados/controller/EmpleadoController.java
- [X] T038 [US3] Emit structured audit events for ACTUALIZACION and ELIMINACION in src/main/java/com/dsw02/empleados/service/EmpleadoServiceImpl.java
- [X] T039 [US3] Add OpenAPI annotations for update/delete endpoints and status codes in src/main/java/com/dsw02/empleados/controller/EmpleadoController.java

**Checkpoint**: CRUD completo por clave compuesta validado por historias.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Cierre transversal de contrato, seguridad, observabilidad y documentación.

- [X] T040 [P] Align implementation with API contract in specs/001-actualizar-clave-compuesta/contracts/empleados.openapi.yaml
- [X] T041 Add integration test for unauthorized access across CRUD endpoints in src/test/java/integration/SecurityCrudIntegrationIT.java
- [X] T042 [P] Add startup/runtime failure logging validation scenario in src/test/java/integration/OperabilityLoggingIntegrationIT.java
- [X] T043 [P] Update quickstart execution notes with implemented commands and checks in specs/001-actualizar-clave-compuesta/quickstart.md
- [X] T044 Run full test suite and record evidence in specs/001-actualizar-clave-compuesta/checklists/requirements.md

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies; starts immediately.
- **Phase 2 (Foundational)**: Depends on Phase 1 and blocks all user stories.
- **Phase 3+ (User Stories)**: Depend on Phase 2 completion.
- **Phase 6 (Polish)**: Depends on all implemented user stories.

### User Story Dependencies

- **US1 (P1)**: Starts after Foundational; independent MVP.
- **US2 (P2)**: Starts after Foundational; reuses base entity/service components.
- **US3 (P3)**: Starts after Foundational; reuses base read/service components.

### Within Each User Story

- Tests before implementation.
- Service/domain before controller wiring.
- OpenAPI endpoint docs completed in the same story phase.

## Parallel Opportunities

- **Setup**: T003, T004, T005 can run in parallel after T001.
- **Foundational**: T009, T010, T011, T014, T015 can run in parallel after T006-T008.
- **US1**: T018, T019, T020 in parallel; then T021-T025 sequence.
- **US2**: T026 and T027 parallel; then T028-T032 sequence.
- **US3**: T033 and T034 parallel; then T035-T039 sequence.
- **Polish**: T040, T042, T043 in parallel; finalize with T044.

---

## Parallel Example: User Story 1

```bash
Task: "T018 [US1] Contract tests for POST /api/empleados in src/test/java/contract/EmpleadoCreateContractIT.java"
Task: "T019 [US1] Integration tests for POST /api/empleados in src/test/java/integration/EmpleadoCreateIntegrationIT.java"
Task: "T020 [US1] Concurrency integration test in src/test/java/integration/EmpleadoCreateConcurrencyIT.java"
```

## Parallel Example: User Story 2

```bash
Task: "T026 [US2] Contract tests for read endpoints in src/test/java/contract/EmpleadoReadContractIT.java"
Task: "T027 [US2] Integration tests for read flows in src/test/java/integration/EmpleadoReadIntegrationIT.java"
```

## Parallel Example: User Story 3

```bash
Task: "T033 [US3] Contract tests for update/delete endpoints in src/test/java/contract/EmpleadoUpdateDeleteContractIT.java"
Task: "T034 [US3] Integration tests for update/delete flows in src/test/java/integration/EmpleadoUpdateDeleteIntegrationIT.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Setup + Foundational (Phases 1-2).
2. Complete User Story 1 (Phase 3).
3. Validate independently with tests T018-T020.
4. Demo/deploy MVP with clave compuesta autogenerada.

### Incremental Delivery

1. Foundation complete.
2. Deliver US1 (alta con clave compuesta) as MVP.
3. Deliver US2 (lecturas por clave compuesta).
4. Deliver US3 (actualizar/eliminar) and polish.

### Suggested MVP Scope

- **MVP recomendado**: Fases 1, 2 y 3 (hasta T025).
- Valor mínimo entregable: alta autenticada con clave `EMP-` + consecutivo sin colisiones y validaciones de negocio.
