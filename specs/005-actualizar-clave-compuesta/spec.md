# Feature Specification: CRUD de Empleados con Clave Compuesta

**Feature Branch**: `005-actualizar-clave-compuesta`  
**Created**: 2026-02-26  
**Status**: Draft  
**Input**: User description: "modifica la especificacion 003-crud-empleados donde el campo clave sea un prefijo EMP- seguido de un auto numerico como PK compuesta"

## User Scenarios & Testing *(mandatory)*

<!--
  IMPORTANT: User stories should be PRIORITIZED as user journeys ordered by importance.
  Each user story/journey must be INDEPENDENTLY TESTABLE - meaning if you implement just ONE of them,
  you should still have a viable MVP (Minimum Viable Product) that delivers value.
  
  Assign priorities (P1, P2, P3, etc.) to each story, where P1 is the most critical.
  Think of each story as a standalone slice of functionality that can be:
  - Developed independently
  - Tested independently
  - Deployed independently
  - Demonstrated to users independently
-->

### User Story 1 - Registrar empleado con clave generada (Priority: P1)

Como usuario autenticado, quiero registrar empleados sin capturar manualmente la clave,
para que el sistema genere una clave compuesta con formato `EMP-` + consecutivo numérico.

**Why this priority**: El alta con clave compuesta define la nueva identidad del empleado y
habilita todos los demás flujos del CRUD.

**Independent Test**: Se valida creando empleados con datos válidos y comprobando que cada alta
regresa una clave con formato `EMP-000001` (prefijo fijo + número incremental), sin duplicados.

**Acceptance Scenarios**:

1. **Given** un usuario autenticado y datos válidos de empleado,
   **When** solicita el alta,
   **Then** el sistema crea el empleado y devuelve una clave compuesta con prefijo `EMP-` y componente numérica autogenerada.
2. **Given** un usuario autenticado,
   **When** intenta crear un empleado con `nombre`, `dirección` o `teléfono` mayores a 100 caracteres,
   **Then** el sistema rechaza la solicitud por validación.
3. **Given** un usuario autenticado,
   **When** intenta enviar manualmente la clave compuesta en el alta,
   **Then** el sistema ignora/rechaza ese valor de entrada y mantiene la generación automática de la clave.

---

### User Story 2 - Consultar empleados por clave compuesta (Priority: P2)

Como usuario autenticado, quiero consultar el listado y el detalle de empleados usando su
clave compuesta para localizar registros con el nuevo esquema de identificación.

**Why this priority**: Permite consumir la información almacenada y comprobar que el nuevo
formato de clave funciona en lectura.

**Independent Test**: Se valida listando empleados y consultando uno existente con su
clave compuesta en formato `EMP-` + consecutivo.

**Acceptance Scenarios**:

1. **Given** empleados registrados,
   **When** el usuario solicita el listado,
   **Then** el sistema devuelve cada empleado incluyendo su clave compuesta.
2. **Given** una clave compuesta existente,
   **When** el usuario solicita el detalle por clave,
   **Then** el sistema devuelve el empleado correspondiente.
3. **Given** una clave compuesta inexistente,
   **When** el usuario solicita el detalle,
   **Then** el sistema responde recurso no encontrado.

---

### User Story 3 - Actualizar y eliminar con clave compuesta (Priority: P3)

Como usuario autenticado, quiero actualizar y eliminar empleados mediante su clave compuesta
para mantener vigente el catálogo con el nuevo esquema de PK.

**Why this priority**: Completa el ciclo CRUD y confirma la consistencia operativa del identificador compuesto.

**Independent Test**: Se valida actualizando y eliminando un empleado existente por su clave compuesta,
y verificando respuestas de error para claves no existentes.

**Acceptance Scenarios**:

1. **Given** una clave compuesta existente,
   **When** el usuario actualiza `nombre`, `dirección` o `teléfono` con valores válidos,
   **Then** el sistema guarda cambios y conserva la misma clave compuesta.
2. **Given** una clave compuesta existente,
   **When** el usuario elimina el empleado,
   **Then** el sistema elimina el registro y deja de retornarlo en consultas.
3. **Given** una clave compuesta inexistente,
   **When** el usuario intenta actualizar o eliminar,
   **Then** el sistema responde recurso no encontrado.

---

### Edge Cases

- Intento de alta con `nombre`, `dirección` o `teléfono` vacíos.
- Intento de alta o actualización con `nombre`, `dirección` o `teléfono` de 101+ caracteres.
- Intento de consulta/actualización/eliminación con clave que no respeta el patrón `EMP-` + dígitos.
- Solicitudes concurrentes de alta que exigen generación de consecutivo sin colisiones.
- Solicitudes sin credenciales válidas en endpoints protegidos.
- Intento de modificar la clave compuesta durante una actualización.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema MUST permitir crear empleados con los campos `nombre`, `dirección` y `teléfono`.
- **FR-002**: El sistema MUST generar una clave compuesta para cada empleado al momento del alta.
- **FR-003**: La clave compuesta MUST estar formada por dos componentes: prefijo fijo `EMP-` y un consecutivo numérico autogenerado.
- **FR-004**: El sistema MUST tratar la composición (`prefijo`, `consecutivo`) como clave primaria compuesta del empleado.
- **FR-005**: El consecutivo numérico MUST ser único por empleado y MUST incrementarse automáticamente para nuevas altas.
- **FR-006**: El sistema MUST NOT permitir que el cliente defina o modifique manualmente la clave compuesta.
- **FR-007**: `nombre`, `dirección` y `teléfono` MUST ser obligatorios, no vacíos y con máximo 100 caracteres.
- **FR-008**: El sistema MUST permitir consultar el listado de empleados mostrando su clave compuesta.
- **FR-009**: El sistema MUST permitir consultar un empleado por su clave compuesta.
- **FR-010**: El sistema MUST permitir actualizar `nombre`, `dirección` y `teléfono` de un empleado existente por clave compuesta.
- **FR-011**: El sistema MUST permitir eliminar un empleado existente por clave compuesta.
- **FR-012**: El sistema MUST responder error de no encontrado cuando la clave compuesta consultada no exista.
- **FR-013**: El sistema MUST proteger las operaciones CRUD con autenticación básica.
- **FR-014**: El sistema MUST mantener documentación actualizada del contrato CRUD en OpenAPI/Swagger.
- **FR-015**: El sistema MUST registrar eventos de alta, actualización y eliminación para trazabilidad operativa.

### Backend Constraints *(mandatory)*

- **BC-001**: La solución MUST ejecutarse en Spring Boot 3 con Java 17.
- **BC-002**: Los endpoints protegidos MUST aplicar autenticación HTTP Basic con comportamiento explícito para autorizado/no autorizado.
- **BC-003**: La persistencia MUST usar PostgreSQL.
- **BC-004**: Los entornos local y CI MUST ejecutar la base de datos mediante contenedores Docker.
- **BC-005**: Los cambios de API MUST incluir actualización del contrato OpenAPI/Swagger.
- **BC-006**: El alcance de pruebas MUST incluir integración para autenticación, persistencia y contrato.

### Key Entities *(include if feature involves data)*

- **Empleado**: Representa un registro del catálogo laboral. Atributos de negocio:
  `nombre`, `dirección`, `teléfono`.
- **ClaveEmpleado**: Identidad compuesta del empleado formada por:
  `prefijo` (valor fijo `EMP-`) + `consecutivo` (número autogenerado incremental).
- **EventoEmpleado**: Registro de trazabilidad para operaciones de alta, actualización y eliminación.

## Assumptions

- El prefijo de clave es fijo y obligatorio: `EMP-`.
- El consecutivo inicia en `1` y se representa con relleno a 6 dígitos para visualización (ejemplo: `EMP-000001`).
- La clave completa visible (`EMP-000001`) es única para cada empleado.
- No se permite editar la clave compuesta después de la creación.
- No se requiere paginación ni filtros avanzados en esta versión.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El 100% de altas válidas generan una clave compuesta con patrón `EMP-` + consecutivo numérico.
- **SC-002**: El 100% de las altas concurrentes en pruebas funcionales evitan colisiones de clave compuesta.
- **SC-003**: El 100% de actualizaciones/eliminaciones sobre claves inexistentes responden con no encontrado.
- **SC-004**: Al menos el 95% de usuarios autenticados completan alta, consulta y actualización básica en menos de 2 minutos durante pruebas funcionales.
