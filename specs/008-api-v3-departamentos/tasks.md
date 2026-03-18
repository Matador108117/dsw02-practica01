# Tasks: API v3 Departamentos y Relacion Empleados

**Input**: Design documents from /specs/008-api-v3-departamentos/
**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/

**Tests**: Se incluyen tareas de pruebas porque la especificacion exige cobertura de integracion y contrato.

**Organization**: Las tareas estan agrupadas por historia de usuario para implementacion y validacion independiente.

## Format: [ID] [P?] [Story] Description

- [P]: Puede ejecutarse en paralelo (archivos distintos, sin dependencia directa)
- [Story]: Etiqueta de historia (US1, US2, US3)
- Cada tarea incluye ruta de archivo exacta

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Preparar base tecnica para endpoints y modelo v3

- [X] T001 Crear DTOs base para departamentos en src/main/java/com/dsw02/empleados/controller/dto/DepartamentoDtos.java
- [X] T002 Crear excepciones de dominio para departamento en src/main/java/com/dsw02/empleados/service/DepartamentoNotFoundException.java y src/main/java/com/dsw02/empleados/service/DepartamentoConflictException.java
- [X] T003 [P] Crear utilitario de paginacion v3 en src/main/java/com/dsw02/empleados/service/PaginationDefaults.java
- [X] T004 [P] Mantener contrato OpenAPI v3 alineado con requisitos base en specs/008-api-v3-departamentos/contracts/empleados-v3.openapi.yaml

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Infraestructura obligatoria que bloquea cualquier historia

**CRITICAL**: No iniciar historias hasta completar esta fase

- [X] T005 Crear entidad Departamento en src/main/java/com/dsw02/empleados/model/Departamento.java
- [X] T006 [P] Crear repositorio Departamento en src/main/java/com/dsw02/empleados/repository/DepartamentoRepository.java
- [X] T007 Extender entidad Empleado con relacion nullable a departamento en src/main/java/com/dsw02/empleados/model/Empleado.java
- [X] T008 Crear migracion de tabla y FK explicita en src/main/resources/db/migration/V10__create_departamento_and_fk_in_empleado.sql
- [X] T009 Crear migracion de backfill seguro para departamento_id en src/main/resources/db/migration/V11__backfill_departamento_id_nullable.sql
- [X] T010 [P] Registrar mapeo de errores 409/422 en src/main/java/com/dsw02/empleados/controller/GlobalExceptionHandler.java
- [X] T011 [P] Ajustar reglas de seguridad para rutas /api/v3/departamentos en src/main/java/com/dsw02/empleados/config/SecurityConfig.java
- [X] T012 [P] Anadir generador de IDs DEP-secuencial en src/main/java/com/dsw02/empleados/service/DepartamentoIdGenerator.java

**Checkpoint**: Base relacional, seguridad y manejo de errores listos para historias

---

## Phase 3: User Story 1 - Gestionar departamentos (Priority: P1) MVP

**Goal**: Entregar CRUD completo de departamentos en /api/v3/departamentos

**Independent Test**: Ejecutar POST/GET/GET{id}/PUT/DELETE de departamentos y verificar 201/200/204 y 409 cuando existan empleados asociados.

### Tests for User Story 1

- [X] T013 [P] [US1] Crear pruebas de contrato CRUD de departamentos en src/test/java/com/dsw02/empleados/contract/DepartamentoCrudContractIT.java
- [X] T014 [P] [US1] Crear pruebas de integracion CRUD y conflicto 409 en src/test/java/com/dsw02/empleados/integration/DepartamentoCrudIntegrationIT.java

### Implementation for User Story 1

- [X] T015 [P] [US1] Crear interfaz de servicio de departamentos en src/main/java/com/dsw02/empleados/service/DepartamentoService.java
- [X] T016 [US1] Implementar servicio de departamentos en src/main/java/com/dsw02/empleados/service/DepartamentoServiceImpl.java
- [X] T017 [US1] Implementar controlador CRUD v3 de departamentos en src/main/java/com/dsw02/empleados/controller/DepartamentoController.java
- [X] T018 [US1] Implementar mapeo DTO<->dominio para departamentos en src/main/java/com/dsw02/empleados/controller/dto/DepartamentoDtos.java
- [X] T019 [US1] Aplicar paginacion default/max en listado de departamentos en src/main/java/com/dsw02/empleados/controller/DepartamentoController.java
- [X] T020 [US1] Sincronizar endpoints CRUD de departamentos en specs/008-api-v3-departamentos/contracts/empleados-v3.openapi.yaml

**Checkpoint**: US1 completa y demostrable de forma independiente

---

## Phase 4: User Story 2 - Consultar empleados por departamento (Priority: P1)

**Goal**: Entregar endpoint relacional obligatorio GET /api/v3/departamentos/{id}/empleados

**Independent Test**: Consultar empleados por departamento existente (con y sin empleados) y validar 404 para departamento inexistente.

### Tests for User Story 2

- [X] T021 [P] [US2] Crear prueba de contrato del endpoint relacional en src/test/java/com/dsw02/empleados/contract/DepartamentoEmpleadosContractIT.java
- [X] T022 [P] [US2] Crear prueba de integracion para listado por departamento en src/test/java/com/dsw02/empleados/integration/DepartamentoEmpleadosIntegrationIT.java

### Implementation for User Story 2

- [X] T023 [P] [US2] Agregar consulta paginada de empleados por departamento en src/main/java/com/dsw02/empleados/repository/EmpleadoRepository.java
- [X] T024 [US2] Implementar metodo de servicio para listado relacional en src/main/java/com/dsw02/empleados/service/DepartamentoServiceImpl.java
- [X] T025 [US2] Exponer endpoint GET /api/v3/departamentos/{id}/empleados en src/main/java/com/dsw02/empleados/controller/DepartamentoController.java
- [X] T026 [US2] Mapear respuesta paginada de empleados para endpoint relacional en src/main/java/com/dsw02/empleados/controller/dto/EmpleadoDtos.java
- [X] T027 [US2] Actualizar contrato del endpoint relacional en specs/008-api-v3-departamentos/contracts/empleados-v3.openapi.yaml

**Checkpoint**: US2 completa e independiente, cumpliendo contrato publico obligatorio

---

## Phase 5: User Story 3 - Asignar y modificar departamento en empleados (Priority: P2)

**Goal**: Permitir departamentoId nullable en POST/PUT de empleados y validar existencia previa

**Independent Test**: Crear/actualizar empleados con departamentoId valido, nulo y no existente (422).

### Tests for User Story 3

- [X] T028 [P] [US3] Extender pruebas de contrato de alta de empleado para departamentoId nullable/422 en src/test/java/com/dsw02/empleados/contract/EmpleadoCreateContractIT.java
- [X] T029 [P] [US3] Extender pruebas de contrato de actualizacion para departamentoId nullable/422 en src/test/java/com/dsw02/empleados/contract/EmpleadoUpdateDeleteContractIT.java
- [X] T030 [P] [US3] Extender pruebas de integracion de creacion para asignacion de departamento en src/test/java/com/dsw02/empleados/integration/EmpleadoCreateIntegrationIT.java
- [X] T031 [P] [US3] Extender pruebas de integracion de actualizacion para reasignacion de departamento en src/test/java/com/dsw02/empleados/integration/EmpleadoUpdateDeleteIntegrationIT.java

### Implementation for User Story 3

- [X] T032 [US3] Extender DTOs de empleado con departamentoId nullable en src/main/java/com/dsw02/empleados/controller/dto/EmpleadoDtos.java
- [X] T033 [US3] Validar existencia de departamento en alta/actualizacion de empleado en src/main/java/com/dsw02/empleados/service/EmpleadoServiceImpl.java
- [X] T034 [US3] Ajustar flujo del controlador de empleado para payload v3 en src/main/java/com/dsw02/empleados/controller/EmpleadoController.java
- [X] T035 [US3] Mapear error de referencia inexistente a 422 en src/main/java/com/dsw02/empleados/controller/GlobalExceptionHandler.java
- [X] T036 [US3] Sincronizar esquemas y respuestas 422 de empleados v3 en specs/008-api-v3-departamentos/contracts/empleados-v3.openapi.yaml

**Checkpoint**: US3 completa e independiente, con validacion semantica y nullability

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Cierre integral de calidad y consistencia entre historias

- [X] T037 [P] Anadir prueba de aplicacion de migraciones v10/v11 en src/test/java/com/dsw02/empleados/integration/FlywayDepartamentosMigrationIT.java
- [X] T038 [P] Anadir regresion de seguridad por rol sobre endpoints v3 en src/test/java/com/dsw02/empleados/integration/SecurityCrudIntegrationIT.java
- [X] T039 [P] Verificar limites de paginacion v3 en src/test/java/com/dsw02/empleados/contract/DepartamentoCrudContractIT.java
- [X] T040 [P] Extender matriz de roles para `POST /api/v3/empleados` y `PUT /api/v3/empleados/{id}` (401/403/201/200) en src/test/java/com/dsw02/empleados/integration/SecurityCrudIntegrationIT.java
- [X] T041 [P] Anadir regresion de autorizacion para USER read-only y ADMIN escritura en endpoints de empleados v3 en src/test/java/com/dsw02/empleados/contract/EmpleadoCreateContractIT.java y src/test/java/com/dsw02/empleados/contract/EmpleadoUpdateDeleteContractIT.java
- [X] T042 [P] Validar no regresion de sunset `410 Gone` para versiones deprecadas en src/test/java/com/dsw02/empleados/integration/ApiVersionSunsetIntegrationIT.java
- [X] T043 [P] Validar evaluacion de cutoff en UTC para sunset de versiones deprecadas en src/test/java/com/dsw02/empleados/service/ApiVersionSupportPolicyServiceTest.java
- [X] T044 [P] Medir objetivo P95 <= 800 ms para `GET /api/v3/departamentos?page=0&size=25` en src/test/java/com/dsw02/empleados/performance/DepartamentoListPerformanceIT.java
- [X] T045 [P] Medir objetivo P95 <= 1000 ms para `GET /api/v3/departamentos/{id}/empleados?page=0&size=25` en src/test/java/com/dsw02/empleados/performance/DepartamentoEmpleadosPerformanceIT.java
- [X] T046 [P] Registrar evidencia de mediciones P95 y entorno en specs/008-api-v3-departamentos/evidence/performance/p95-baseline.md
- [X] T047 Actualizar guia de verificacion de feature en specs/008-api-v3-departamentos/quickstart.md
- [X] T048 Consolidar notas de entrega y evidencia de pruebas en specs/008-api-v3-departamentos/plan.md
- [X] T049 [P] Agregar prueba de integracion para mapeo de Basic Auth username a correo_electronico (BC-002a) en src/test/java/com/dsw02/empleados/integration/BasicAuthUsernameMappingIntegrationIT.java
- [X] T050 [P] Agregar prueba de integracion para validacion por hash de contrasena en autenticacion (BC-002b) en src/test/java/com/dsw02/empleados/integration/BasicAuthHashValidationIntegrationIT.java
- [X] T051 [P] Agregar prueba de contrato para asegurar que contrasena solo es input y no se expone en responses (BC-011/BC-012) en src/test/java/com/dsw02/empleados/contract/EmpleadoCredentialContractIT.java
- [X] T052 Implementar/reforzar validacion de Basic Auth por correo_electronico y PasswordEncoder en src/main/java/com/dsw02/empleados/service/EmpleadoUserDetailsService.java y src/main/java/com/dsw02/empleados/config/SecurityConfig.java
- [X] T053 Implementar/reforzar persistencia exclusiva de contrasena_hash sin plaintext (BC-011/BC-012) en src/main/java/com/dsw02/empleados/service/EmpleadoServiceImpl.java y src/main/java/com/dsw02/empleados/controller/dto/EmpleadoDtos.java
- [X] T054 [P] Agregar prueba de integracion para verificar que no se persiste ni retorna contrasena plaintext (BC-011/BC-012) en src/test/java/com/dsw02/empleados/integration/EmpleadoCreateIntegrationIT.java

---

## Dependencies & Execution Order

### Phase Dependencies

- Phase 1 (Setup): inicia inmediatamente.
- Phase 2 (Foundational): depende de Phase 1 y bloquea todo desarrollo de historias.
- Phase 3 (US1): depende de Phase 2.
- Phase 4 (US2): depende de Phase 2 y reutiliza componentes de servicio/controlador de US1.
- Phase 5 (US3): depende de Phase 2 y del modelo relacional establecido en US1.
- Phase 6 (Polish): depende de historias completadas.

### User Story Dependencies

- US1 (P1): primera entrega MVP.
- US2 (P1): depende de base relacional y componentes de departamento creados en US1.
- US3 (P2): depende de integracion Empleado-Departamento y validaciones de US1.

### Within Each User Story

- Pruebas de contrato/integracion primero (deben fallar antes de implementar).
- Repositorio/modelo antes de servicio.
- Servicio antes de controlador.
- Controlador antes de sincronizacion de contrato OpenAPI.

## Parallel Opportunities

- Setup: T003 y T004 en paralelo tras T001/T002.
- Foundational: T006, T010, T011 y T012 en paralelo tras T005.
- US1: T013 y T014 en paralelo; T015 y T018 pueden correr en paralelo.
- US2: T021 y T022 en paralelo; T023 y T027 en paralelo.
- US3: T028-T031 en paralelo; T032 y T036 en paralelo.
- Polish: T037-T046 en paralelo (respetando serializacion por archivo compartido).

---

## Parallel Example: User Story 1

```bash
# Tests US1 en paralelo
T013 src/test/java/com/dsw02/empleados/contract/DepartamentoCrudContractIT.java
T014 src/test/java/com/dsw02/empleados/integration/DepartamentoCrudIntegrationIT.java

# Implementacion US1 en paralelo
T015 src/main/java/com/dsw02/empleados/service/DepartamentoService.java
T018 src/main/java/com/dsw02/empleados/controller/dto/DepartamentoDtos.java
```

## Parallel Example: User Story 2

```bash
# Tests US2 en paralelo
T021 src/test/java/com/dsw02/empleados/contract/DepartamentoEmpleadosContractIT.java
T022 src/test/java/com/dsw02/empleados/integration/DepartamentoEmpleadosIntegrationIT.java

# Implementacion US2 en paralelo
T023 src/main/java/com/dsw02/empleados/repository/EmpleadoRepository.java
T027 specs/008-api-v3-departamentos/contracts/empleados-v3.openapi.yaml
```

## Parallel Example: User Story 3

```bash
# Tests US3 en paralelo
T028 src/test/java/com/dsw02/empleados/contract/EmpleadoCreateContractIT.java
T029 src/test/java/com/dsw02/empleados/contract/EmpleadoUpdateDeleteContractIT.java
T030 src/test/java/com/dsw02/empleados/integration/EmpleadoCreateIntegrationIT.java
T031 src/test/java/com/dsw02/empleados/integration/EmpleadoUpdateDeleteIntegrationIT.java

# Implementacion US3 en paralelo
T032 src/main/java/com/dsw02/empleados/controller/dto/EmpleadoDtos.java
T036 specs/008-api-v3-departamentos/contracts/empleados-v3.openapi.yaml
```

---

## Implementation Strategy

### MVP First (US1)

1. Completar Phase 1 y Phase 2.
2. Completar US1 (Phase 3).
3. Validar independencia de US1 (CRUD + 409 en delete con dependencias).
4. Demostrar MVP.

### Incremental Delivery

1. Base comun: Setup + Foundational.
2. Incremento 1: US1 (CRUD Departamentos).
3. Incremento 2: US2 (endpoint relacional obligatorio).
4. Incremento 3: US3 (asignacion de departamento en empleados).
5. Cierre: Polish con regresiones y evidencia final.

### Parallel Team Strategy

1. Equipo completo en Setup + Foundational.
2. Luego distribucion:
   - Dev A: US1
   - Dev B: US2
   - Dev C: US3
3. Integracion final en Phase 6.

---

## Notes

- Las tareas sin etiqueta [US] pertenecen a Setup, Foundational o Polish.
- Evitar mezclar cambios de historias distintas en un mismo commit.
- Validar quickstart y contrato OpenAPI antes de cerrar la feature.
