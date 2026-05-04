# Feature Specification: CRUD de Empleados

**Feature Branch**: `003-crud-empleados`  
**Created**: 2026-02-25  
**Status**: Draft  
**Input**: User description: "Crea un crud de empleados con los campos clave, nombre, dirección y teléfono. Donde clave sea un prefijo EMP- seguido de un autonumérico como PK compuesta y nombre, dirección y teléfono sea de 100 caracteres."

## Clarifications

### Session 2026-02-25

- Q: ¿Cómo se define `clave` como identificador principal? → A: `clave` se forma automáticamente como PK compuesta (`prefijo` fijo `EMP-` + componente autonumérica).
- Q: ¿`nombre`, `dirección` y `teléfono` son obligatorios? → A: Sí, los tres son obligatorios y no vacíos.
- Q: ¿Qué estructura debe tener `clave`? → A: `clave` visible debe respetar el formato `EMP-` + consecutivo numérico (ejemplo `EMP-000001`).

## User Scenarios & Testing *(mandatory)*


### User Story 1 - Registrar empleado (Priority: P1)

Como usuario autenticado, quiero registrar un empleado con nombre,
dirección y teléfono para que el sistema le asigne una clave compuesta.

**Why this priority**: Sin creación no existe información para consultar,
actualizar o eliminar; es el flujo mínimo de valor.

**Independent Test**: Se prueba creando un empleado válido y verificando que
queda almacenado con una clave compuesta con formato `EMP-` + consecutivo y campos de texto hasta 100 caracteres.

**Acceptance Scenarios**:

1. **Given** un usuario autenticado,
   **When** envía un alta con nombre, dirección y teléfono válidos,
   **Then** el sistema registra el empleado, genera automáticamente la clave compuesta y devuelve confirmación.
2. **Given** un usuario autenticado,
   **When** intenta registrar un empleado con nombre, dirección o teléfono con más de 100 caracteres,
   **Then** el sistema rechaza la solicitud con mensaje de validación.
3. **Given** un usuario autenticado,
   **When** intenta registrar un empleado enviando manualmente una clave,
   **Then** el sistema rechaza o ignora ese valor y mantiene la generación automática.

---

### User Story 2 - Consultar empleados (Priority: P2)

Como usuario autenticado, quiero consultar el listado y el detalle de empleados
para localizar información por clave compuesta de forma rápida.

**Why this priority**: Permite consumir el valor del registro y validar la
calidad de los datos almacenados.

**Independent Test**: Se prueba listando empleados y consultando un empleado
existente por clave compuesta sin depender de actualización o eliminación.

**Acceptance Scenarios**:

1. **Given** empleados previamente registrados,
   **When** el usuario solicita el listado,
   **Then** el sistema devuelve la colección de empleados con clave, nombre, dirección y teléfono.
2. **Given** una clave compuesta existente,
   **When** el usuario solicita el detalle por clave,
   **Then** el sistema devuelve los datos completos del empleado.
3. **Given** una clave compuesta inexistente,
   **When** el usuario solicita el detalle por clave,
   **Then** el sistema responde que el recurso no fue encontrado.

---

### User Story 3 - Actualizar y eliminar empleado (Priority: P3)

Como usuario autenticado, quiero actualizar y eliminar empleados para mantener
el catálogo vigente y libre de registros obsoletos.

**Why this priority**: Completa el ciclo CRUD y permite mantener datos correctos.

**Independent Test**: Se prueba actualizando nombre/dirección/teléfono de un
empleado y eliminando uno existente, verificando resultados en consultas.

**Acceptance Scenarios**:

1. **Given** una clave compuesta existente,
   **When** el usuario actualiza nombre, dirección o teléfono con hasta 100 caracteres,
   **Then** el sistema guarda los cambios correctamente.
2. **Given** una clave compuesta existente,
   **When** el usuario intenta actualizar nombre, dirección o teléfono con más de 100 caracteres,
   **Then** el sistema rechaza la solicitud por validación.
3. **Given** una clave compuesta existente,
   **When** el usuario solicita eliminar el empleado,
   **Then** el sistema elimina el registro y deja de retornarlo en consultas.

---

### Edge Cases


- Intento de crear un empleado con campos obligatorios vacíos.
- Intento de manipular manualmente la clave compuesta en alta o actualización.
- Intento de capturar nombre, dirección o teléfono con 101 o más caracteres.
- Intento de consultar, actualizar o eliminar un empleado con clave compuesta inexistente.
- Solicitudes sin credenciales válidas en endpoints protegidos.

## Requirements *(mandatory)*


### Functional Requirements

- **FR-001**: El sistema MUST permitir crear empleados con los campos
  `nombre`, `dirección` y `teléfono`.
- **FR-002**: El sistema MUST generar automáticamente la clave del empleado al crear un registro.
- **FR-002a**: La clave MUST tener formato `EMP-` + consecutivo numérico.
- **FR-002b**: El sistema MUST tratar la clave como PK compuesta (`prefijo`, `consecutivo`).
- **FR-002c**: El sistema MUST NOT permitir que la clave sea capturada o modificada manualmente por el usuario.
- **FR-003**: El sistema MUST validar que `nombre`, `dirección` y `teléfono`
  tengan un máximo de 100 caracteres cada uno.
- **FR-003a**: El sistema MUST requerir `nombre`, `dirección` y `teléfono`
  como campos obligatorios y no vacíos.
- **FR-004**: El sistema MUST rechazar altas y actualizaciones cuando
  `nombre`, `dirección` o `teléfono` excedan 100 caracteres.
- **FR-005**: El sistema MUST permitir consultar el listado de empleados.
- **FR-006**: El sistema MUST permitir consultar un empleado por su `clave` compuesta.
- **FR-007**: El sistema MUST permitir actualizar un empleado existente por su `clave` compuesta.
- **FR-008**: El sistema MUST permitir eliminar un empleado existente por su `clave` compuesta.
- **FR-009**: El sistema MUST responder con error de recurso no encontrado
  cuando se consulte, actualice o elimine una `clave` compuesta inexistente.
- **FR-010**: El sistema MUST proteger las operaciones CRUD de empleados con
  autenticación básica.
- **FR-011**: El sistema MUST mantener documentación actualizada del contrato
  del CRUD de empleados en Swagger/OpenAPI.
- **FR-012**: El sistema MUST registrar eventos de alta, actualización y
  eliminación para trazabilidad operativa.

### Backend Constraints *(mandatory)*

- **BC-001**: La solución MUST cumplir el estándar de plataforma backend
  establecido por la constitución del proyecto.
- **BC-002**: Los endpoints protegidos MUST mantener un mecanismo de
  autenticación básica coherente con la política del proyecto.
- **BC-003**: La persistencia de datos MUST usar el motor relacional definido
  como estándar en el proyecto.
- **BC-004**: Los entornos de desarrollo y CI MUST usar un entorno de base de
  datos reproducible mediante contenedores.
- **BC-005**: Todo cambio de API MUST incluir actualización de documentación
  formal del contrato para consumidores.
- **BC-006**: El alcance de pruebas MUST incluir integración para autenticación,
  persistencia y contrato de API del CRUD de empleados.

### Key Entities *(include if feature involves data)*

- **Empleado**: Representa a una persona registrada en el catálogo de empleados.
  Atributos clave: `clave` (compuesta por `prefijo` fijo `EMP-` + `consecutivo` numérico autogenerado, identificador único),
  `nombre` (texto obligatorio, máximo 100), `dirección` (texto obligatorio, máximo 100),
  `teléfono` (texto obligatorio, máximo 100).

## Assumptions

- El CRUD opera para usuarios autenticados y con permisos de gestión del catálogo.
- La clave compuesta identifica de forma única a cada empleado en toda la colección.
- El componente consecutivo inicia en 1 y se incrementa automáticamente por cada alta.
- No se requiere recuperación de registros eliminados en esta versión.
- No se solicita paginación/filtros avanzados; el listado base es suficiente para el MVP.

## Success Criteria *(mandatory)*


### Measurable Outcomes

- **SC-001**: El 100% de intentos de creación y actualización con `nombre`,
  `dirección` o `teléfono` de más de 100 caracteres son rechazados con error de validación.
- **SC-002**: El 100% de operaciones sobre `clave` compuesta inexistente devuelven un
  resultado de no encontrado.
- **SC-003**: Al menos el 95% de los usuarios autenticados completan una alta de
  empleado válida en menos de 1 minuto durante pruebas funcionales.
- **SC-004**: Al menos el 95% de operaciones CRUD válidas finalizan en menos de
  2 segundos en entorno de prueba del proyecto.
