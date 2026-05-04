# Feature Specification: API v3 Departamentos y Relacion Empleados

**Feature Branch**: `008-api-v3-departamentos`  
**Created**: 2026-03-17  
**Status**: Draft  
**Input**: User description: "Definir especificacion API v3 para integrar departamentos, FK en empleados, endpoint relacional departamentos/{id}/empleados y ajustes de empleados con validacion de existencia"

## Clarifications

### Session 2026-03-17

- Q: Que politica aplica al eliminar departamentos con empleados asociados? -> A: Rechazar eliminacion por conflicto de relacion
- Q: Que parametros de paginacion aplican a colecciones en v3? -> A: page=0, size=25, maxSize=100
- Q: Que codigo HTTP aplica cuando `departamento_id` no existe en POST/PUT de empleados? -> A: 422 Unprocessable Entity
- Q: Cual es el formato canonico del identificador de departamentos? -> A: DEP-<numero_secuencial_de_6_digitos> con padding fijo exacto
- Q: Que codigo HTTP aplica al eliminar un departamento con empleados asociados? -> A: 409 Conflict

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Gestionar departamentos (Priority: P1)

Como administrador del sistema, necesito crear, consultar, actualizar y eliminar departamentos para mantener una estructura organizacional consistente.

**Why this priority**: La nueva entidad `Departamento` es el cambio base que habilita todos los demas cambios de modelo y API v3.

**Independent Test**: Puede probarse de forma aislada ejecutando solo el CRUD de departamentos sobre `/api/v3/departamentos` y verificando persistencia y reglas de validacion.

**Acceptance Scenarios**:

1. **Given** que no existe un departamento con nombre "Finanzas", **When** se solicita crear "Finanzas", **Then** el departamento se registra con un identificador valido y queda disponible para consultas.
2. **Given** que existe un departamento, **When** se consulta por su ID, **Then** la respuesta devuelve su informacion completa.
3. **Given** que existe un departamento, **When** se actualiza su nombre, **Then** la respuesta refleja el nuevo nombre.
4. **Given** que existe un departamento sin empleados asociados, **When** se elimina, **Then** deja de estar disponible en consultas posteriores.

---

### User Story 2 - Consultar empleados por departamento (Priority: P1)

Como consumidor de la API v3, necesito consultar todos los empleados asociados a un departamento para obtener una vista organizacional por area.

**Why this priority**: El endpoint relacional es un requisito obligatorio del contrato publico v3.

**Independent Test**: Puede probarse de forma independiente con datos semilla que incluyan varios empleados en un mismo departamento y ejecutando `GET /api/v3/departamentos/{id}/empleados`.

**Acceptance Scenarios**:

1. **Given** que un departamento tiene empleados asociados, **When** se consulta `/api/v3/departamentos/{id}/empleados`, **Then** la respuesta devuelve la lista completa de empleados pertenecientes a ese departamento.
2. **Given** que un departamento existe pero no tiene empleados asociados, **When** se consulta el endpoint relacional, **Then** la respuesta devuelve una lista vacia.
3. **Given** un ID de departamento inexistente, **When** se consulta el endpoint relacional, **Then** la API responde con error de recurso no encontrado.

---

### User Story 3 - Asignar y modificar departamento en empleados (Priority: P2)

Como administrador, necesito crear y actualizar empleados con o sin `departamento_id` para reflejar incorporaciones pendientes de asignacion o cambios de area.

**Why this priority**: Extiende funcionalidad existente de empleados y depende del modelo de departamentos ya disponible.

**Independent Test**: Puede validarse con operaciones de alta y modificacion de empleados en v3, probando casos con `departamento_id` nulo, valido e invalido.

**Acceptance Scenarios**:

1. **Given** un departamento valido existente, **When** se crea un empleado con `departamento_id` informado, **Then** el empleado queda asociado correctamente.
2. **Given** un alta de empleado sin `departamento_id`, **When** se procesa la creacion, **Then** el empleado se persiste sin asociacion y la operacion es valida.
3. **Given** un empleado existente, **When** se actualiza su `departamento_id` a otro departamento existente, **Then** la asociacion se actualiza correctamente.
4. **Given** un `departamento_id` inexistente, **When** se intenta crear o actualizar un empleado con ese valor, **Then** la API rechaza la solicitud por referencia invalida.

### Edge Cases

- Intento de crear un departamento con `nombre` vacio o ausente.
- Intento de usar `departamento_id` con formato invalido en endpoints de empleados.
- Consulta relacional a un departamento eliminado concurrentemente.
- Eliminacion de departamento con empleados asociados (debe respetar integridad referencial y comunicar error de conflicto de relacion).
- Migracion en entorno con empleados existentes: todos deben quedar con `departamento_id` nulo si no hay regla de asignacion previa.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema MUST exponer la API publica de esta capacidad bajo el prefijo `/api/v3`.
- **FR-002**: El sistema MUST soportar `POST /api/v3/departamentos` para crear departamentos con `nombre` obligatorio y no vacio.
- **FR-003**: El sistema MUST soportar `GET /api/v3/departamentos` para listar departamentos.
- **FR-004**: El sistema MUST soportar `GET /api/v3/departamentos/{id}` para recuperar un departamento por identificador.
- **FR-005**: El sistema MUST soportar `PUT /api/v3/departamentos/{id}` para actualizar los datos de un departamento existente.
- **FR-006**: El sistema MUST soportar `DELETE /api/v3/departamentos/{id}` para eliminar un departamento aplicando las reglas de integridad de relacion definidas para dependencias.
- **FR-007**: El sistema MUST incluir el endpoint obligatorio `GET /api/v3/departamentos/{id}/empleados` en el contrato publico v3.
- **FR-008**: El endpoint relacional MUST devolver exclusivamente empleados cuyo `departamento_id` coincide con el ID consultado.
- **FR-009**: Los endpoints v3 de creacion de empleados MUST permitir `departamento_id` nulo o ausente.
- **FR-010**: Los endpoints v3 de creacion de empleados MUST aceptar `departamento_id` cuando exista el departamento referenciado.
- **FR-011**: Los endpoints v3 de actualizacion de empleados MUST permitir cambiar `departamento_id` a un valor valido o nulo.
- **FR-011a**: En el contrato API, el campo de payload `departamentoId` MUST mapearse al campo de persistencia `departamento_id`.
- **FR-012**: El sistema MUST validar la existencia del departamento antes de persistir creaciones o actualizaciones de empleados que incluyan `departamento_id`.
- **FR-013**: Cuando el departamento referenciado no exista, el sistema MUST rechazar la solicitud de empleados con un error de validacion de referencia.
- **FR-014**: La migracion de datos MUST crear la tabla `departamentos` con `id` como clave primaria y `nombre` requerido.
- **FR-015**: La migracion de datos MUST modificar `empleados` agregando `departamento_id` nullable.
- **FR-016**: La migracion de datos MUST definir una clave foranea explicita `empleados.departamento_id -> departamentos.id`.
- **FR-017**: La base de datos MUST hacer cumplir integridad referencial para la relacion empleado-departamento.
- **FR-018**: La migracion MUST incluir una estrategia de backfill segura para registros existentes; cuando no exista origen confiable de asignacion, el valor inicial de `departamento_id` sera nulo.
- **FR-019**: La documentacion publica MUST declarar oficialmente esta evolucion como API v3 por cambio de modelo, relacion y expansion de contrato.
- **FR-020**: Cuando existan empleados asociados a un departamento, la eliminacion MUST rechazarse con `409 Conflict`, sin nullificacion automatica ni cascada implicita.
- **FR-021**: Los endpoints de coleccion en v3 MUST soportar parametros de paginacion `page` y `size`.
- **FR-022**: El valor por defecto de paginacion en v3 MUST ser `page=0` y `size=25`.
- **FR-023**: El tamano maximo de pagina permitido en v3 MUST ser `size=100`.
- **FR-024**: Cuando `departamento_id` no exista en POST/PUT de empleados en v3, la API MUST responder `422 Unprocessable Entity`.
- **FR-025**: El identificador de `departamentos.id` MUST seguir el formato `DEP-<numero_secuencial_de_6_digitos>` con padding fijo exacto, por ejemplo `DEP-000001`.
- **FR-026**: Cuando ocurra `409 Conflict` por eliminacion de departamento con dependencias, la API MUST devolver un error explicito de conflicto de relacion trazable para cliente y auditoria.

### Backend Constraints *(mandatory)*

- **BC-001**: Solution MUST run on Spring Boot 3 with Java 17.
- **BC-002**: Protected endpoints MUST define mandatory HTTP Basic (`type=http`, `scheme=basic`) authentication behavior.
- **BC-002a**: Basic Auth username MUST map to persisted `correo_electronico`.
- **BC-002b**: Basic Auth password MUST be transient input and MUST be validated by comparing derived hash against persisted `contrasena_hash`.
- **BC-003**: Authorization MUST enforce `USER` read-only access and `ADMIN` full CRUD access.
- **BC-004**: Data persistence MUST target PostgreSQL.
- **BC-005**: Local and CI database execution MUST be Docker-based.
- **BC-006**: API changes MUST include OpenAPI/Swagger documentation updates.
- **BC-007**: Spec MUST state required integration tests for auth, role authorization, DB, and API contract.
- **BC-008**: Public API endpoints MUST be versioned with `/api/v{major}` and breaking changes MUST declare migration impact.
- **BC-008a**: Any public contract expansion or addition of public endpoints MUST trigger a major API version increment.
- **BC-009**: Collection endpoints MUST define pagination parameters plus default and maximum page limits.
- **BC-010**: Implementation workflow MUST define feature branch, PR traceability, and expected commit granularity.
- **BC-011**: The `empleado` persistence model/table MUST enforce required `correo_electronico` and `contrasena_hash` attributes.
- **BC-012**: `contrasena` MUST be treated as input-only and MUST NOT be persisted in plaintext.
- **BC-013**: Deprecated API versions past sunset MUST respond `410 Gone`, with UTC as the business clock for cutoff evaluation.
- **BC-014**: Model integrity for this scope MUST enforce `Departamento (1) -> (N) Empleados`, each empleado belongs to at most one departamento, and employee department assignment MAY be null.
- **BC-015**: Implicit relationships without declared FK constraints are forbidden.

### Key Entities *(include if feature involves data)*

- **Departamento**: Unidad organizacional identificada por `id` con formato canonico `DEP-<numero_secuencial_de_6_digitos>` con padding fijo exacto y atributo `nombre` obligatorio (maximo 150 caracteres).
- **Empleado (v3)**: Registro de empleado que incorpora `departamento_id` opcional para asociacion a un `Departamento`.
- **Relacion Empleado-Departamento**: Relacion `1:N` desde `Departamento` hacia `Empleado`, con FK explicita en `Empleado` y enforcement de integridad en base de datos.

## Assumptions

- Los permisos y autenticacion de endpoints v3 mantienen el modelo ya vigente (USER lectura, ADMIN escritura), salvo nueva definicion explicita posterior.
- El formato de `departamentos.id` se define en esta especificacion como `DEP-<numero_secuencial_de_6_digitos>` con padding fijo exacto (6 digitos).
- En los payloads API v3 se usa `departamentoId`, mapeado internamente a `departamento_id` en persistencia.
- La eliminacion de departamentos con empleados asociados sera rechazada por conflicto de relacion, sin nullificacion automatica ni eliminacion en cascada.
- Los endpoints de empleados de versiones anteriores mantienen su contrato y no son parte del alcance de esta especificacion.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El 100% de operaciones CRUD de departamentos en v3 se completan con respuestas correctas para casos validos en pruebas de aceptacion.
- **SC-002**: El 100% de solicitudes de empleados en v3 con `departamento_id` inexistente son rechazadas con error de validacion de referencia.
- **SC-003**: El endpoint `GET /api/v3/departamentos/{id}/empleados` retorna resultados correctos (incluyendo lista vacia cuando aplica) en al menos el 95% de escenarios de prueba definidos sin defectos funcionales.
- **SC-004**: La migracion se ejecuta sin perdida de registros existentes y con 0 violaciones de integridad referencial en validaciones post-migracion.
- **SC-005**: La documentacion publica identifica la version v3 y cubre el 100% de endpoints nuevos/modificados antes de liberar la feature.
