# Tasks: Correccion de acceso global en Swagger UI

**Input**: Design documents from `/specs/001-swagger-global-auth/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Se incluyen tareas de pruebas porque la especificacion exige pruebas de integracion para bloqueo global, autenticacion centralizada, invalidacion por `401` y usuario de prueba (BC-008, BC-008a).

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Preparar base de configuracion para seguridad global en Swagger y bootstrap de usuario de prueba.

- [X] T001 Alinear metadata de la feature y referencias de implementacion en `specs/001-swagger-global-auth/plan.md`
- [X] T002 Verificar dependencias de seguridad/OpenAPI/Flyway en `pom.xml`
- [X] T003 [P] Alinear propiedades de seguridad y arranque en `src/main/resources/application.yml`
- [X] T004 [P] Confirmar runtime DB local/CI para pruebas de seguridad en `docker/docker-compose.yml`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Infraestructura transversal que bloquea todas las historias.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T005 Crear migracion para transicion de `contrasena` a `contrasena_hash` en `src/main/resources/db/migration/V4__migrate_contrasena_to_hash.sql`
- [ ] T006 [P] Crear migracion para normalizacion/indice unico case-insensitive de correo en `src/main/resources/db/migration/V5__normalize_unique_correo.sql`
- [ ] T007 [P] Actualizar entidad persistida para `contrasena_hash` y campos requeridos en `src/main/java/com/dsw02/empleados/model/Empleado.java`
- [ ] T008 [P] Ajustar DTOs para mantener `contrasena` solo como entrada y evitar exposicion en salida en `src/main/java/com/dsw02/empleados/controller/dto/EmpleadoDtos.java`
- [ ] T009 Implementar codificacion/verificacion de contrasena con hash en `src/main/java/com/dsw02/empleados/config/SecurityConfig.java`
- [ ] T010 [P] Implementar resolucion de identidad por `correo_electronico` desde persistencia en `src/main/java/com/dsw02/empleados/service/EmpleadoUserDetailsService.java`
- [ ] T011 [P] Actualizar contrato OpenAPI para seguridad global y excepcion de lectura de `/v3/api-docs` en `specs/001-swagger-global-auth/contracts/swagger-global-auth-v2.openapi.yaml`
- [ ] T012 Agregar manejo de errores de autenticacion/authorization consistente para flujos Swagger en `src/main/java/com/dsw02/empleados/controller/GlobalExceptionHandler.java`

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - Autorizacion unica en Swagger (Priority: P1) 🎯 MVP

**Goal**: Garantizar bloqueo inicial global y autenticar una sola vez desde Authorize para reutilizar credenciales en endpoints protegidos.

**Independent Test**: Abrir Swagger sin sesion, validar bloqueo total, autenticar una vez con credenciales validas y ejecutar multiples endpoints protegidos sin login por metodo.

### Tests for User Story 1

- [ ] T013 [P] [US1] Crear prueba de integracion de bloqueo inicial de endpoints protegidos en `src/test/java/com/dsw02/empleados/integration/SwaggerGlobalLockIntegrationIT.java`
- [ ] T014 [P] [US1] Crear prueba de integracion de autenticacion unica y reutilizacion de credenciales en `src/test/java/com/dsw02/empleados/integration/SwaggerSingleAuthorizeIntegrationIT.java`
- [ ] T015 [P] [US1] Crear prueba contractual de esquema `basicAuth` global y seguridad en operaciones protegidas en `src/test/java/com/dsw02/empleados/contract/SwaggerSecurityContractIT.java`

### Implementation for User Story 1

- [ ] T016 [US1] Aplicar seguridad global HTTP Basic a operaciones protegidas en `src/main/java/com/dsw02/empleados/config/SecurityConfig.java`
- [ ] T017 [US1] Ajustar configuracion OpenAPI para declarar `basicAuth` global sin repeticion por operacion en `src/main/java/com/dsw02/empleados/config/OpenApiConfig.java`
- [ ] T018 [US1] Eliminar configuraciones de autenticacion redundante por endpoint en `src/main/java/com/dsw02/empleados/controller/EmpleadoController.java`
- [ ] T019 [US1] Asegurar respuesta `401` en credencial invalida para re-Authorize desde Swagger en `src/main/java/com/dsw02/empleados/config/SecurityConfig.java`
- [ ] T020 [US1] Actualizar guia de validacion funcional del flujo Authorize unico en `specs/001-swagger-global-auth/quickstart.md`

**Checkpoint**: User Story 1 should be fully functional and testable independently

---

## Phase 4: User Story 2 - Bloqueo global consistente (Priority: P2)

**Goal**: Asegurar coherencia entre contrato OpenAPI, filtros backend y comportamiento de ejecucion protegida.

**Independent Test**: Verificar que la seguridad declarada en OpenAPI coincide con enforcement backend y que llamadas protegidas sin credenciales son bloqueadas antes del controlador.

### Tests for User Story 2

- [ ] T021 [P] [US2] Crear prueba contractual para validar ausencia de seguridad por operacion redundante en `src/test/java/com/dsw02/empleados/contract/OpenApiGlobalSecurityConsistencyIT.java`
- [ ] T022 [P] [US2] Crear prueba de integracion que valide bloqueo previo al controlador en `src/test/java/com/dsw02/empleados/integration/SecurityFilterPreControllerIT.java`
- [ ] T023 [P] [US2] Crear prueba de integracion para invalidez de sesion tras primer `401` en `src/test/java/com/dsw02/empleados/integration/SwaggerReauthorizeAfter401IT.java`
- [ ] T040 [P] [US2] Crear prueba de integracion de matriz de roles (`ADMIN` CRUD, `USER` solo lectura) en `src/test/java/com/dsw02/empleados/integration/RoleMatrixIntegrationIT.java`
- [ ] T041 [P] [US2] Crear prueba de integracion para validar login como unica excepcion publica de negocio, permitir excepcion tecnica documentada de lectura para `/v3/api-docs` y denegacion uniforme del resto sin auth en `src/test/java/com/dsw02/empleados/integration/LoginExemptionIntegrationIT.java`

### Implementation for User Story 2

- [ ] T024 [US2] Restringir endpoints protegidos para exigir autenticacion en cada request backend en `src/main/java/com/dsw02/empleados/config/SecurityConfig.java`
- [ ] T025 [US2] Permitir lectura de descriptor API sin habilitar ejecucion protegida en `src/main/java/com/dsw02/empleados/config/SecurityConfig.java`
- [ ] T026 [US2] Añadir trazas estructuradas para intentos protegidos rechazados y `401` en `src/main/java/com/dsw02/empleados/config/SecurityConfig.java`
- [ ] T027 [US2] Sincronizar contrato de respuestas `200/401` para endpoints protegidos en `specs/001-swagger-global-auth/contracts/swagger-global-auth-v2.openapi.yaml`
- [ ] T042 [US2] Implementar reglas explicitas de autorizacion por rol para asegurar `ADMIN` CRUD y `USER` solo lectura en `src/main/java/com/dsw02/empleados/config/SecurityConfig.java`
- [ ] T043 [US2] Configurar matcher explicito para login como unica excepcion publica de negocio y para endpoints tecnicos de documentacion en modo solo lectura (por ejemplo `/v3/api-docs`) en `src/main/java/com/dsw02/empleados/config/SecurityConfig.java`

**Checkpoint**: User Stories 1 and 2 should both work independently

---

## Phase 5: User Story 3 - Usuario de prueba reutilizable (Priority: P3)

**Goal**: Crear/normalizar en arranque el usuario de prueba obligatorio de forma idempotente y segura.

**Independent Test**: Ejecutar arranque con base vacia y con usuario preexistente incompleto; validar creacion/normalizacion sin duplicados y autenticacion exitosa del usuario de prueba.

### Tests for User Story 3

- [ ] T028 [P] [US3] Crear prueba de integracion de bootstrap en base vacia en `src/test/java/com/dsw02/empleados/integration/TestUserBootstrapCreateIT.java`
- [ ] T029 [P] [US3] Crear prueba de integracion de normalizacion idempotente de usuario existente en `src/test/java/com/dsw02/empleados/integration/TestUserBootstrapNormalizeIT.java`
- [ ] T030 [P] [US3] Crear prueba de integracion para login exitoso del usuario de prueba en `src/test/java/com/dsw02/empleados/integration/TestUserAuthIntegrationIT.java`

### Implementation for User Story 3

- [ ] T031 [US3] Implementar inicializador interno de arranque para crear/normalizar usuario de prueba en `src/main/java/com/dsw02/empleados/config/TestUserBootstrapRunner.java`
- [ ] T032 [US3] Implementar logica idempotente de conciliacion por email canonico en `src/main/java/com/dsw02/empleados/service/EmpleadoServiceImpl.java`
- [ ] T033 [US3] Garantizar persistencia exclusiva de `contrasena_hash` en bootstrap en `src/main/java/com/dsw02/empleados/config/TestUserBootstrapRunner.java`
- [ ] T034 [US3] Documentar credenciales de prueba y validaciones de arranque en `specs/001-swagger-global-auth/quickstart.md`

**Checkpoint**: All user stories should now be independently functional

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Cierre transversal de calidad, seguridad y trazabilidad.

- [ ] T035 [P] Ejecutar alineacion final de contrato y anotaciones Swagger/OpenAPI en `src/main/java/com/dsw02/empleados/config/OpenApiConfig.java`
- [ ] T036 [P] Endurecer regresiones de seguridad para evitar login por operacion en `src/test/java/com/dsw02/empleados/integration/SwaggerSecurityRegressionIT.java`
- [ ] T037 [P] Verificar que no se introducen rutas `/api/v3/*` y mantener alcance en v2 en `src/test/java/com/dsw02/empleados/contract/ApiVersionScopeContractIT.java`
- [ ] T038 Consolidar ejecucion de pruebas de contrato e integracion en CI en `.github/workflows/ci.yml`
- [ ] T039 Validar quickstart end-to-end y registrar evidencia final en `specs/001-swagger-global-auth/checklists/requirements.md`
- [ ] T044 [P] Añadir prueba de regresion de baseline historica (bloqueo global, Authorize unico, sin login por operacion) en `src/test/java/com/dsw02/empleados/integration/SwaggerBaselineConsistencyIT.java`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies
- **Phase 2 (Foundational)**: Depends on Phase 1 and blocks all user stories
- **Phase 3 (US1)**: Depends on Phase 2
- **Phase 4 (US2)**: Depends on Phase 2; valida consistencia sobre base de US1
- **Phase 5 (US3)**: Depends on Phase 2; reutiliza seguridad/base de identidad
- **Phase 6 (Polish)**: Depends on completion of US1, US2, US3

### User Story Dependencies

- **US1 (P1)**: Base MVP de autenticacion centralizada
- **US2 (P2)**: Depende funcionalmente de reglas de seguridad global ya operativas en US1
- **US3 (P3)**: Depende de infraestructura de seguridad/base de datos pero puede validarse de forma independiente sobre el bootstrap

### Dependency Graph

- Setup -> Foundational -> US1 -> US2 -> Polish
- Setup -> Foundational -> US3 -> Polish

### Within Each User Story

- Escribir pruebas primero y validarlas en fallo inicial
- Implementar configuracion/modelo antes que controladores
- Cerrar historia con validacion independiente completa

### Parallel Opportunities

- T003 y T004 en paralelo en Setup
- T006, T007, T008, T010 y T011 en paralelo en Foundational
- T013, T014 y T015 en paralelo en US1
- T021, T022 y T023 en paralelo en US2
- T040 y T041 en paralelo en US2
- T028, T029 y T030 en paralelo en US3
- T035, T036, T037 y T044 en paralelo en Polish

---

## Parallel Example: User Story 1

```bash
Task: "T013 [US1] SwaggerGlobalLockIntegrationIT en src/test/java/com/dsw02/empleados/integration/SwaggerGlobalLockIntegrationIT.java"
Task: "T014 [US1] SwaggerSingleAuthorizeIntegrationIT en src/test/java/com/dsw02/empleados/integration/SwaggerSingleAuthorizeIntegrationIT.java"
Task: "T015 [US1] SwaggerSecurityContractIT en src/test/java/com/dsw02/empleados/contract/SwaggerSecurityContractIT.java"
```

## Parallel Example: User Story 2

```bash
Task: "T021 [US2] OpenApiGlobalSecurityConsistencyIT en src/test/java/com/dsw02/empleados/contract/OpenApiGlobalSecurityConsistencyIT.java"
Task: "T022 [US2] SecurityFilterPreControllerIT en src/test/java/com/dsw02/empleados/integration/SecurityFilterPreControllerIT.java"
Task: "T023 [US2] SwaggerReauthorizeAfter401IT en src/test/java/com/dsw02/empleados/integration/SwaggerReauthorizeAfter401IT.java"
Task: "T040 [US2] RoleMatrixIntegrationIT en src/test/java/com/dsw02/empleados/integration/RoleMatrixIntegrationIT.java"
Task: "T041 [US2] LoginExemptionIntegrationIT en src/test/java/com/dsw02/empleados/integration/LoginExemptionIntegrationIT.java"
```

## Parallel Example: User Story 3

```bash
Task: "T028 [US3] TestUserBootstrapCreateIT en src/test/java/com/dsw02/empleados/integration/TestUserBootstrapCreateIT.java"
Task: "T029 [US3] TestUserBootstrapNormalizeIT en src/test/java/com/dsw02/empleados/integration/TestUserBootstrapNormalizeIT.java"
Task: "T030 [US3] TestUserAuthIntegrationIT en src/test/java/com/dsw02/empleados/integration/TestUserAuthIntegrationIT.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Completar Phase 1 + Phase 2.
2. Completar US1.
3. Validar criterios SC-001, SC-002 y SC-003.
4. Demostrar flujo Authorize unico en Swagger.

### Incremental Delivery

1. Entregar US1 (autenticacion global centralizada).
2. Entregar US2 (consistencia contrato/backend y re-Authorize por `401`).
3. Entregar US3 (bootstrap idempotente del usuario de prueba).
4. Ejecutar Polish y evidencia final.

### Suggested MVP Scope

- **MVP recomendado**: Phase 1 + Phase 2 + Phase 3 (US1)
- Valor minimo: bloqueo global + una sola autenticacion centralizada en Swagger con enforcement backend

---

## Requirement Traceability (Cross-Cutting Tasks)

- T001 -> FR-016, BC-009
- T003 -> BC-001, BC-003
- T004 -> BC-001
- T012 -> BC-003, BC-004
- T038 -> BC-008, BC-008a
- T039 -> SC-001, SC-002, SC-003, SC-004a, SC-005, SC-006a
