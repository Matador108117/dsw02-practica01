# Tasks: Gestion de credenciales y roles de empleado

**Input**: Design documents from `/specs/004-empleado-auth-roles/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Se incluyen tareas de pruebas porque la especificacion exige cobertura de integracion para autenticacion, autorizacion por rol, DB y contrato API (BC-007), mas validacion de rendimiento en CI (BC-019).

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Preparar base tecnica y operativa para auth por correo, roles, versionado v1/v2 y politicas UTC.

- [x] T001 Alinear configuracion de DB, seguridad y variables de bootstrap in src/main/resources/application.yml
- [x] T002 [P] Verificar dependencias de seguridad, validacion, OpenAPI y pruebas in pom.xml
- [x] T003 [P] Alinear metadata OpenAPI base para versionado y seguridad in src/main/java/com/dsw02/empleados/config/OpenApiConfig.java
- [x] T004 [P] Ajustar runtime Docker PostgreSQL local/CI in docker/docker-compose.yml

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Infraestructura transversal obligatoria para todas las historias.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [x] T005 Crear migracion incremental hash-only (sin sobrescribir V3 existente) para transicionar `contrasena` legacy -> `contrasena_hash` y validar integridad post-migracion in src/main/resources/db/migration/V4__migrate_contrasena_to_contrasena_hash.sql
- [x] T006 [P] Crear migracion de unicidad case-insensitive por correo in src/main/resources/db/migration/V5__add_unique_lower_correo_electronico.sql
- [x] T007 [P] Crear migracion de tabla de intentos de autenticacion in src/main/resources/db/migration/V6__create_auth_attempt_table.sql
- [x] T008 Crear migracion de `ApiVersionSupportPolicy` con `release_v2_at_utc` y `sunset_at_utc` in src/main/resources/db/migration/V7__create_api_version_support_policy.sql
- [x] T009 [P] Crear migracion idempotente de backfill de rol `USER` para registros existentes in src/main/resources/db/migration/V8__backfill_default_user_role.sql
- [x] T010 [P] Implementar entidad AuthAttempt in src/main/java/com/dsw02/empleados/model/AuthAttempt.java
- [x] T011 [P] Implementar entidad ApiVersionSupportPolicy in src/main/java/com/dsw02/empleados/model/ApiVersionSupportPolicy.java
- [x] T012 [P] Crear AuthAttemptRepository in src/main/java/com/dsw02/empleados/repository/AuthAttemptRepository.java
- [x] T013 [P] Crear ApiVersionSupportPolicyRepository in src/main/java/com/dsw02/empleados/repository/ApiVersionSupportPolicyRepository.java
- [x] T014 Actualizar entidad Empleado para persistencia hash-only in src/main/java/com/dsw02/empleados/model/Empleado.java
- [x] T015 Ajustar DTOs para `contrasena` solo de entrada y salida sin credencial in src/main/java/com/dsw02/empleados/controller/dto/EmpleadoDtos.java
- [x] T016 Implementar UserDetailsService persistente por correo in src/main/java/com/dsw02/empleados/service/EmpleadoUserDetailsService.java
- [x] T017 Implementar PasswordEncoder y AuthenticationProvider in src/main/java/com/dsw02/empleados/config/SecurityConfig.java
- [x] T018 [P] Implementar AuthAttemptService (5/15 + bloqueo 15) in src/main/java/com/dsw02/empleados/service/AuthAttemptService.java
- [x] T019 [P] Implementar ApiVersionSupportPolicyService con logica UTC/sunset in src/main/java/com/dsw02/empleados/service/ApiVersionSupportPolicyService.java
- [x] T020 Implementar bootstrap `ADMIN` controlado (secreto en variable segura, unico bootstrap, forzar cambio en primer login, registrar evento) in src/main/java/com/dsw02/empleados/config/SecurityConfig.java
- [x] T021 [P] Agregar validacion automatizada de naming convention de ramas (`feature/*`, `fix/*`, `chore/*`) in .github/workflows/ci.yml
- [x] T022 [P] Crear template obligatorio de PR con trazabilidad task->branch->PR y checklist constitucional in .github/pull_request_template.md
- [x] T023 [P] Agregar verificacion de atomicidad/no mezcla de concerns en pipeline de revision in .github/workflows/ci.yml

**Checkpoint**: Foundation ready - user story implementation can now begin.

---

## Phase 3: User Story 1 - Autenticacion con correo y contrasena (Priority: P1) 🎯 MVP

**Goal**: Habilitar autenticacion robusta por correo/contrasena con hash irreversible, lockout y semantica `401/423`.

**Independent Test**: Credenciales validas permiten acceso; credenciales invalidas responden `401`; bloqueo activo responde `423`; no se persiste contrasena en texto plano; y el backfill no deja registros sin rol.

### Tests for User Story 1

- [ ] T024 [P] [US1] Actualizar contrato de autenticacion para `401` y `423` in src/test/java/com/dsw02/empleados/contract/EmpleadoReadContractIT.java
- [ ] T025 [P] [US1] Cubrir lockout por correo+IP y desbloqueo temporal in src/test/java/com/dsw02/empleados/integration/SecurityCrudIntegrationIT.java
- [ ] T026 [P] [US1] Ajustar concurrencia de altas para correo unico in src/test/java/com/dsw02/empleados/integration/EmpleadoCreateConcurrencyIT.java
- [ ] T027 [P] [US1] Verificar que `contrasena` no se persiste en texto plano in src/test/java/com/dsw02/empleados/integration/EmpleadoCreateIntegrationIT.java
- [ ] T028 [P] [US1] Verificar backfill de rol idempotente y ausencia de registros sin rol in src/test/java/com/dsw02/empleados/integration/RoleBackfillIntegrationIT.java

### Implementation for User Story 1

- [ ] T029 [US1] Implementar hash-only en altas/actualizaciones in src/main/java/com/dsw02/empleados/service/EmpleadoServiceImpl.java
- [ ] T030 [US1] Integrar AuthAttemptService y respuestas `401/423` in src/main/java/com/dsw02/empleados/config/SecurityConfig.java
- [ ] T031 [US1] Normalizar correo para auth y unicidad in src/main/java/com/dsw02/empleados/service/EmpleadoServiceImpl.java
- [ ] T032 [US1] Registrar eventos de autenticacion (exito/fallo) y autorizacion denegada con `timestamp_utc`, `userId`, `resultado`, `origen_solicitud` in src/main/java/com/dsw02/empleados/config/SecurityConfig.java
- [ ] T033 [US1] Aplicar reglas base de autorizacion por rol in src/main/java/com/dsw02/empleados/config/SecurityConfig.java

**Checkpoint**: User Story 1 implementada y validable de forma independiente.

---

## Phase 4: User Story 2 - Rol admin con CRUD completo (Priority: P2)

**Goal**: Asegurar CRUD completo para `ADMIN` en v2 y ciclo de deprecacion v1 con enforcement UTC y `410` post-sunset.

**Independent Test**: `ADMIN` ejecuta CRUD completo; `v1` responde en ventana de deprecacion con headers observables y devuelve `410 Gone` al superar sunset.

### Tests for User Story 2

- [ ] T034 [P] [US2] Actualizar contrato de create/list v2 con payload de credenciales y rol in src/test/java/com/dsw02/empleados/contract/EmpleadoCreateContractIT.java
- [ ] T035 [P] [US2] Validar contractualmente headers `Deprecation: true` y `Sunset` en v1 in src/test/java/com/dsw02/empleados/contract/EmpleadoReadContractIT.java
- [ ] T036 [P] [US2] Agregar contrato `410 Gone` para v1 post-sunset in src/test/java/com/dsw02/empleados/contract/EmpleadoReadContractIT.java
- [ ] T037 [P] [US2] Cubrir CRUD ADMIN completo in src/test/java/com/dsw02/empleados/integration/EmpleadoUpdateDeleteIntegrationIT.java
- [ ] T038 [P] [US2] Verificar disponibilidad pre-sunset e indisponibilidad post-sunset de v1 in src/test/java/com/dsw02/empleados/integration/EmpleadoReadIntegrationIT.java
- [ ] T039 [P] [US2] Agregar prueba de inmutabilidad de `release_v2_at_utc` in src/test/java/com/dsw02/empleados/integration/VersionPolicyIntegrationIT.java

### Implementation for User Story 2

- [ ] T040 [US2] Implementar endpoints versionados v1/v2 incluyendo headers medibles de deprecacion (`Deprecation`, `Sunset`) in src/main/java/com/dsw02/empleados/controller/EmpleadoController.java
- [ ] T041 [US2] Implementar enforcement UTC de sunset y respuesta `410` in src/main/java/com/dsw02/empleados/controller/EmpleadoController.java
- [ ] T042 [US2] Implementar persistencia/lectura runtime de ApiVersionSupportPolicy in src/main/java/com/dsw02/empleados/service/ApiVersionSupportPolicyService.java
- [ ] T043 [US2] Completar CRUD ADMIN con validaciones de negocio y `409` por correo in src/main/java/com/dsw02/empleados/service/EmpleadoServiceImpl.java
- [ ] T044 [US2] Actualizar contrato OpenAPI v1/v2 con headers de deprecacion, sunset UTC y `410` in specs/004-empleado-auth-roles/contracts/empleados-auth-v2.openapi.yaml

**Checkpoint**: User Stories 1 y 2 funcionales y testeables de forma independiente.

---

## Phase 5: User Story 3 - Rol user solo consultas (Priority: P3)

**Goal**: Garantizar que `USER` mantiene acceso solo lectura en v1/v2 sin efectos laterales en escrituras.

**Independent Test**: `USER` puede GET y recibe rechazo consistente en POST/PUT/DELETE sin cambios de estado.

### Tests for User Story 3

- [ ] T045 [P] [US3] Agregar pruebas de read-only para USER in src/test/java/com/dsw02/empleados/integration/SecurityCrudIntegrationIT.java
- [ ] T046 [P] [US3] Verificar lectura paginada USER in src/test/java/com/dsw02/empleados/integration/EmpleadoReadIntegrationIT.java
- [ ] T047 [P] [US3] Verificar respuestas `403` para escrituras USER in src/test/java/com/dsw02/empleados/contract/EmpleadoUpdateDeleteContractIT.java

### Implementation for User Story 3

- [ ] T048 [US3] Endurecer reglas de autorizacion por metodo HTTP para USER in src/main/java/com/dsw02/empleados/config/SecurityConfig.java
- [ ] T049 [US3] Garantizar no-efecto lateral en escrituras denegadas in src/main/java/com/dsw02/empleados/service/EmpleadoServiceImpl.java
- [ ] T050 [US3] Registrar eventos de autorizacion denegada USER in src/main/java/com/dsw02/empleados/controller/GlobalExceptionHandler.java
- [ ] T051 [US3] Endurecer politicas read-only de USER en rutas v1/v2 y validarlas en filtros de seguridad in src/main/java/com/dsw02/empleados/config/SecurityConfig.java

**Checkpoint**: Todas las historias funcionales con matriz de roles aplicada.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Cierre transversal con evidencia de contrato, rendimiento y cumplimiento constitucional.

- [ ] T052 [P] Sincronizar quickstart con flujo sunset UTC y `410` post-sunset in specs/004-empleado-auth-roles/quickstart.md
- [ ] T053 [P] Validar consistencia de paginacion y defaults (`size`<=100, default 20) in src/main/java/com/dsw02/empleados/controller/EmpleadoController.java
- [ ] T054 [P] Implementar pruebas de carga P95 auth<=2000ms y listados<=800ms in src/test/java/com/dsw02/empleados/integration/PerformanceIT.java
- [ ] T055 Configurar pipeline CI para ejecutar PerformanceIT, publicar reporte de percentiles como artefacto y bloquear merge/promocion cuando P95 auth>2000ms o P95 listados>800ms in .github/workflows/ci.yml
- [ ] T056 [P] Agregar evidencia en CI del backfill de rol (ningun registro sin rol) in .github/workflows/ci.yml
- [ ] T057 Ejecutar suite completa y corregir regresiones in src/test/java/com/dsw02/empleados/integration/
- [ ] T058 Actualizar checklist de evidencia final con enlaces a artefactos CI de rendimiento/backfill/workflow in specs/004-empleado-auth-roles/checklists/requirements.md
- [ ] T059 Declarar cierre formal de feature mediante acta tecnica breve in specs/004-empleado-auth-roles/checklists/feature-closeout.md
- [ ] T060 Registrar commit etiquetado de cierre formal de feature in specs/004-empleado-auth-roles/checklists/feature-closeout.md
- [ ] T061 Registrar aprobacion del comite tecnico de cierre in specs/004-empleado-auth-roles/checklists/feature-closeout.md
- [ ] T062 Registrar decision explicita de politica de version (sin cambio mayor en esta iteracion) in specs/004-empleado-auth-roles/checklists/feature-closeout.md

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies.
- **Foundational (Phase 2)**: Depends on Setup completion; blocks all user stories.
- **User Stories (Phase 3+)**: Depend on Foundational completion.
- **Polish (Phase 6)**: Depends on all user stories complete.

### User Story Dependencies

- **US1 (P1)**: Starts after Foundational; no dependency on US2/US3.
- **US2 (P2)**: Starts after Foundational and depends on US1 auth base.
- **US3 (P3)**: Starts after Foundational and validates role boundaries built in US1/US2.

### Dependency Graph

- Foundational -> US1 -> US2 -> US3 -> Polish
- Regla de orden: bootstrap ADMIN y backfill de rol ocurren en Foundational; US3 no introduce dependencias inversas sobre US2.

### Within Each User Story

- Tests first (contract + integration), then implementation.
- Models/repositories before service logic.
- Service logic before endpoint wiring.
- Security/version enforcement before docs and quickstart evidence.

## Parallel Opportunities

- **Setup**: T002, T003, T004 can run in parallel after T001.
- **Foundational**: T006, T007, T009, T010, T011, T012, T018, T019, T021, T022, T023 can run in parallel after T005/T008/T020.
- **US1**: T024, T025, T026, T027, T028 can run in parallel.
- **US2**: T034, T035, T036, T037, T038, T039 can run in parallel.
- **US3**: T045, T046, T047 can run in parallel.
- **Polish**: T052, T053, T054, T056 can run in parallel before T055/T057-T062.

---

## Parallel Example: User Story 1

```bash
Task: "T024 [US1] Contrato 401/423 in src/test/java/com/dsw02/empleados/contract/EmpleadoReadContractIT.java"
Task: "T025 [US1] Lockout integration in src/test/java/com/dsw02/empleados/integration/SecurityCrudIntegrationIT.java"
Task: "T027 [US1] Hash-only persistence in src/test/java/com/dsw02/empleados/integration/EmpleadoCreateIntegrationIT.java"
```

## Parallel Example: User Story 2

```bash
Task: "T036 [US2] Contrato 410 post-sunset in src/test/java/com/dsw02/empleados/contract/EmpleadoReadContractIT.java"
Task: "T038 [US2] Prueba pre/post sunset in src/test/java/com/dsw02/empleados/integration/EmpleadoReadIntegrationIT.java"
Task: "T039 [US2] Inmutabilidad release_v2_at_utc in src/test/java/com/dsw02/empleados/integration/VersionPolicyIntegrationIT.java"
```

## Parallel Example: User Story 3

```bash
Task: "T045 [US3] USER read-only integration in src/test/java/com/dsw02/empleados/integration/SecurityCrudIntegrationIT.java"
Task: "T046 [US3] USER read paginado in src/test/java/com/dsw02/empleados/integration/EmpleadoReadIntegrationIT.java"
Task: "T047 [US3] Contrato 403 USER write in src/test/java/com/dsw02/empleados/contract/EmpleadoUpdateDeleteContractIT.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 and Phase 2.
2. Complete Phase 3 (US1).
3. Validate auth/lockout/hash-only (`401` vs `423`).
4. Demo/deploy MVP slice.

### Incremental Delivery

1. Foundation complete (Phases 1-2).
2. Deliver US1 (auth and lockout).
3. Deliver US2 (admin CRUD + sunset UTC/410 + policy runtime).
4. Deliver US3 (user read-only enforcement).
5. Execute polish (performance evidence + CI gates + cierre formal).

### Suggested MVP Scope

- **MVP recomendado**: Phase 1 + Phase 2 + Phase 3 (US1).
- Entrega minima de valor: autenticacion segura por correo con hash-only, lockout y semantica HTTP verificable.
