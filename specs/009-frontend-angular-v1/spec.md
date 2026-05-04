# Feature Specification: Frontend Angular V1

**Feature Branch**: `009-frontend-angular-v1`  
**Created**: 2026-03-19  
**Status**: Implemented  
**Input**: User description: "Frontend oficial Angular 22 LTS con login obligatorio, dashboard CRUD empleados/departamentos, Docker Compose y Cypress E2E"

## Clarifications

### Session 2026-03-19

- Q: Para autenticacion de frontend, cual estrategia se adopta para evitar almacenar credenciales en cliente? -> A: Opcion C, implementar JWT + refresh (`/api/v4/auth/refresh`) con expiracion de 12 horas y coexistencia segura Basic + JWT bajo gobernanza explicita.
- Q: Donde se almacenan JWT y refresh token en cliente web? -> A: Opcion A, en cookies `HttpOnly` + `Secure` + `SameSite`, con estado de sesion de UI en memoria.
- Q: Cual politica CSRF se adopta usando cookies de autenticacion? -> A: Opcion B, proteccion CSRF explicita con token para `/auth/refresh`, logout y endpoints CRUD sensibles.
- Q: Cual semantica HTTP se adopta para resultado de login? -> A: Opcion B ajustada a politica cookie-first, `200 OK` en login valido con payload `{ status, role }` y emision de tokens solo por cookies seguras; `401 Unauthorized` en invalido sin token en payload.
- Q: Como se resuelve la persistencia de sesion frente al requisito de 12 horas? -> A: Opcion C, access token en sesion y refresh token persistente por 12 horas.

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

### User Story 1 - Autenticacion y Acceso Seguro (Priority: P1)

Como usuario del sistema, quiero iniciar sesion desde una pantalla obligatoria de login
para acceder al resto de pantallas segun mi rol, sin exponer credenciales en URL.

**Why this priority**: Sin control de acceso no existe uso seguro del frontend ni
cumplimiento de politicas de autenticacion.

**Independent Test**: Puede validarse de forma independiente intentando acceder a una
ruta protegida sin autenticacion, ejecutando login valido/invalido y comprobando
redireccion, estado de sesion y rol resultante.

**Acceptance Scenarios**:

1. **Given** un usuario no autenticado, **When** intenta abrir cualquier pantalla distinta
  al login, **Then** el sistema redirige a login de forma inmediata.
2. **Given** un usuario en login con campos vacios, **When** intenta autenticarse,
  **Then** el frontend bloquea el envio y muestra validaciones de campos obligatorios.
3. **Given** credenciales validas, **When** el usuario inicia sesion,
  **Then** el sistema marca sesion activa y habilita navegacion a pantallas protegidas.
4. **Given** credenciales invalidas, **When** el usuario inicia sesion,
  **Then** el acceso se rechaza y el usuario permanece en login con mensaje claro.

---

### User Story 2 - Dashboard de Entidades (Priority: P2)

Como usuario autenticado, quiero visualizar un dashboard con sidebar, encabezado,
subtitulo y tabla dinamica para consultar empleados y departamentos desde una
interfaz responsive.

**Why this priority**: Entrega valor de negocio inmediato al habilitar visualizacion
y navegacion del dominio principal.

**Independent Test**: Puede validarse sobre una baseline con US1 completada,
comprobando layout, selector de entidad, buscador, render de tablas y footer.

**Acceptance Scenarios**:

1. **Given** un usuario autenticado, **When** abre el dashboard,
  **Then** se muestra el layout base en grid con header, sidebar, subtitulo,
  area principal y footer.
2. **Given** el sidebar visible, **When** el usuario selecciona Empleados o
  Departamentos, **Then** cambia la entidad activa y se renderiza la tabla
  correspondiente con encabezados y filas responsivas.
3. **Given** la entidad Departamentos seleccionada, **When** el usuario activa
  "Ver empleados del departamento", **Then** el sistema muestra empleados
  asociados al departamento seleccionado.

---

### User Story 3 - Operaciones CRUD con Toggles (Priority: P3)

Como usuario autorizado, quiero ejecutar operaciones de agregar, editar y eliminar
desde tarjetas toggle para gestionar entidades de manera consistente.

**Why this priority**: Completa la operacion funcional del frontend sobre el dominio
sin duplicar reglas de negocio del backend.

**Independent Test**: Puede validarse sobre baseline con US1 y US2 completadas,
ejecutando flujo CRUD basico desde tarjetas toggle y comprobando resultado en tabla.

**Acceptance Scenarios**:

1. **Given** una tabla cargada, **When** el usuario activa el toggle Agregar,
   **Then** puede registrar una entidad y ver la tabla actualizada.
2. **Given** una fila existente, **When** el usuario activa el toggle Editar,
   **Then** puede modificar la entidad y confirmar el cambio en la tabla.
3. **Given** una fila existente, **When** el usuario activa el toggle Eliminar,
   **Then** puede confirmar borrado y ver la entidad removida de la tabla.

---

### Edge Cases

- Navegacion directa a ruta interna con sesion expirada o inexistente.
- Respuesta de autenticacion con rol no reconocido por frontend.
- Fallo temporal de API durante login o carga de tablas.
- Solicitud sensible sin token CSRF valido con cookies de sesion activas.
- Reapertura del navegador con access token de sesion ausente y refresh token vigente.
- Consulta de departamentos sin empleados asociados.
- Operacion CRUD rechazada por permisos del rol autenticado.
- Cambio de tamano de pantalla que obliga colapso del sidebar en movil.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema MUST presentar una pantalla inicial de login obligatoria.
- **FR-002**: El sistema MUST redirigir a login cualquier acceso a ruta protegida
  cuando el usuario no este autenticado.
- **FR-003**: El login MUST solicitar email y contrasena como campos obligatorios
  y MUST validar no-vacio en frontend antes del envio.
- **FR-004**: La validacion real de credenciales MUST realizarse via API.
- **FR-005**: Las credenciales MUST enviarse en cuerpo JSON y MUST NOT enviarse en URL.
- **FR-006**: El sistema MUST exponer contrato canonico de autenticacion en API v4:
  `POST /api/v4/auth/login`, `POST /api/v4/auth/refresh` y `POST /api/v4/auth/logout`.
- **FR-007**: El sistema MUST implementar autenticacion con token JWT para frontend,
  con expiracion maxima de 12 horas y renovacion mediante `POST /api/v4/auth/refresh`.
- **FR-008**: El frontend MUST mantener sesion hasta logout explicito o expiracion
  efectiva del refresh token (maximo 12 horas); si refresh falla, la sesion MUST
  terminar de inmediato.
- **FR-009**: El frontend MUST almacenar JWT y refresh token en cookies
  `HttpOnly` + `Secure` + `SameSite`; la UI MUST mantener solo estado de sesion
  en memoria y MUST NOT persistir credenciales o tokens en `localStorage`.
- **FR-010**: El dashboard MUST implementar layout base en grid con seis areas:
  contenedor principal, footer, sidebar, header, subtitulo y area de tabla.
- **FR-011**: El header MUST mostrar el titulo "Front para dsw02-practica01" centrado.
- **FR-012**: El sidebar MUST listar entidades disponibles desde API, incluir buscador,
  resaltar elemento activo y ofrecer accion visible de cierre de sesion.
- **FR-013**: El subtitulo MUST reflejar la entidad actualmente seleccionada.
- **FR-014**: El area principal MUST renderizar dinamicamente tabla de Empleados o
  Departamentos con comportamiento responsive.
- **FR-015**: Bajo la tabla MUST existir toggles de acciones CRUD: Agregar, Editar,
  Eliminar.
- **FR-016**: Para Departamentos MUST existir toggle adicional para ver empleados de
  departamento usando `GET /api/v3/departamentos/{id}/empleados`.
- **FR-017**: El footer MUST mostrar version de frontend centrada.
- **FR-018**: El frontend MUST aplicar paleta y estilo visual minimalista con estados
  hover/activo claramente diferenciados, contraste minimo WCAG AA (>= 4.5:1)
  para texto principal y transiciones de interfaz entre 100 ms y 250 ms.
- **FR-019**: El frontend MUST ser responsive y colapsar sidebar en pantallas moviles.
- **FR-020**: El frontend MUST tener Dockerfile propio, build de produccion, puerto
  configurable e integracion como servicio adicional en docker-compose existente.
- **FR-021**: El sistema MUST ejecutar pruebas Cypress E2E obligatorias para login
  valido, login invalido, render de empleados, render de departamentos, CRUD basico
  y consulta de empleados por departamento.
- **FR-022**: Ningun build MUST considerarse valido sin exito de la suite E2E requerida.
- **FR-023**: El frontend MUST mantener versionado independiente del backend con formato
  recomendado `vMAJOR.MINOR.PATCH-front`.
- **FR-024**: Esta iniciativa MUST usar API v4 para endpoints de autenticacion y
  mantener endpoints de dominio existentes en v3 sin regresion funcional.
- **FR-025**: La coexistencia Basic + JWT MUST estar formalmente aprobada por
  gobernanza constitucional como cambio separado del feature.
- **FR-026**: `POST /api/v4/auth/login` MUST responder `200 OK` con payload de exito
  (`status`, `role`) donde `status=ACCEPTED`, `role in {USER,ADMIN}` y MUST NOT
  incluir campos `token` ni `refreshToken`; MUST establecer tokens unicamente por
  cookies seguras
  (`HttpOnly` + `Secure` + `SameSite`) en autenticacion valida; MUST responder
  `401 Unauthorized` con error estructurado y MUST NOT incluir `token` ni
  `refreshToken` en payload en autenticacion invalida.
- **FR-027**: Las cookies de autenticacion MUST invalidarse en logout y el backend
  MUST rechazar refresh para sesiones revocadas o expiradas usando
  `POST /api/v4/auth/logout` y `POST /api/v4/auth/refresh`.
- **FR-028**: El sistema MUST implementar proteccion CSRF explicita (token anti-CSRF)
  para operaciones sensibles cuando se usen cookies de autenticacion, incluyendo
  `/api/v4/auth/refresh`, `/api/v4/auth/logout` y endpoints CRUD.
- **FR-029**: El access token MUST manejarse como sesion no persistente y el refresh
  token MUST poder persistir hasta 12 horas para rehidratacion de sesion entre
  recarga, cierre y reapertura del navegador.

### Endpoint Exposure Policy

- **EP-001**: `POST /api/v4/auth/login` SHALL ser endpoint publico de bootstrap de
  autenticacion y MUST aceptar solo JSON en cuerpo.
- **EP-002**: `POST /api/v4/auth/refresh` SHALL ser endpoint publico-controlado,
  protegido por cookie segura y token CSRF obligatorio.
- **EP-003**: `POST /api/v4/auth/logout` SHALL ser endpoint autenticado para
  invalidacion de sesion y revocacion de refresh.
- **EP-004**: Endpoints de dominio (`/api/v3/empleados/**`,
  `/api/v3/departamentos/**`) SHALL mantener politica de autorizacion por rol
  existente sin degradacion de seguridad.

### Backend Constraints *(mandatory)*

- **BC-001**: Solution MUST run on Spring Boot 3 with Java 17.
- **BC-002**: Protected endpoints MUST define mandatory HTTP Basic
  (`type=http`, `scheme=basic`) authentication behavior.
- **BC-002a**: Basic Auth username MUST map to persisted `correo_electronico`.
- **BC-002b**: Basic Auth password MUST be transient input and MUST be validated
  by comparing derived hash against persisted `contrasena_hash`.
- **BC-003**: Authorization MUST enforce `USER` read-only access and `ADMIN` full CRUD access.
- **BC-004**: Data persistence MUST target PostgreSQL.
- **BC-005**: Local and CI database execution MUST be Docker-based.
- **BC-006**: API changes MUST include OpenAPI/Swagger documentation updates.
- **BC-007**: Spec MUST state required integration tests for auth, role authorization,
  DB, and API contract.
- **BC-008**: Public API endpoints MUST be versioned with `/api/v{major}` and
  breaking changes MUST declare migration impact.
- **BC-008a**: Cualquier ajuste de autenticacion para frontend MUST priorizar
  compatibilidad con la politica de seguridad vigente en backend.
- **BC-009**: Collection endpoints MUST define pagination parameters plus default and
  maximum page limits.
- **BC-010**: Implementation workflow MUST define feature branch, PR traceability,
  and expected commit granularity.
- **BC-011**: The `empleado` persistence model/table MUST enforce required
  `correo_electronico` and `contrasena_hash` attributes.
- **BC-012**: `contrasena` MUST be treated as input-only and MUST NOT be persisted
  in plaintext.
- **BC-013**: Deprecated API versions past sunset MUST respond `410 Gone`, with UTC
  as the business clock for cutoff evaluation.
- **BC-014**: When `Departamento` is in scope, model integrity MUST enforce:
  `Departamento (1) -> (N) Empleados`, employee belongs to at most one department,
  and employee department assignment MAY be null.
- **BC-015**: Implicit relationships without declared FK constraints are forbidden.

### Frontend Constraints *(mandatory when official frontend is in scope)*

- **FC-001**: Official frontend implementation MUST use Angular 22 LTS.
- **FC-002**: Frontend codebase MUST use TypeScript.
- **FC-003**: Node version management MUST use nvm.
- **FC-004**: Frontend MUST consume only official API endpoints.
- **FC-005**: Frontend MUST NOT duplicate backend business logic.
- **FC-006**: Frontend authentication flow MUST be delegated to the API.
- **FC-007**: Frontend delivery MUST include Docker containerization.
- **FC-008**: Frontend services MUST integrate with the existing docker-compose stack.
- **FC-009**: Cypress MUST be the E2E framework for frontend testing.
- **FC-010**: Cypress execution in CI MUST publish test evidence artifacts
  (videos, screenshots y reporte JUnit/JSON) for failed and passed runs.
- **FC-011**: Frontend CI pipeline MUST fail when E2E execution is missing
  mandatory evidence artifacts.
- **FC-012**: Frontend release process MUST generate immutable image tags from
  frontend package version and git SHA.

### Assumptions

- Se asume que Empleados y Departamentos seguiran siendo entidades oficiales del
  dashboard y pueden crecer segun endpoints oficiales disponibles.
- Se asume que el backend resolvera autenticacion via `POST /api/v4/auth/login`
  sin exponer credenciales en URL.
- Se asume que el backend soportara coexistencia de Basic Auth y JWT sin romper
  contratos API vigentes de dominio en `/api/v3`.
- Se asume que "almacenamiento seguro" en navegador significa evitar persistencia de
  credenciales en texto plano y usar cookies `HttpOnly` + `Secure` + `SameSite`.
- Se asume que el access token se mantiene en sesion no persistente y que el refresh
  token puede persistir hasta 12 horas para restaurar sesion de forma controlada.
- Se asume que la introduccion de auth en v4 y la continuidad de endpoints de dominio
  en v3 queda aprobada para esta entrega.
- Se define como entorno de prueba para SC de performance: stack docker-compose local
  con servicios `api`, `postgres` y `frontend`, ejecutando en el mismo host con al
  menos 2 vCPU y 4 GB RAM disponibles para la prueba.

### Key Entities *(include if feature involves data)*

- **SesionAutenticada**: Representa estado de acceso del usuario en frontend,
  incluyendo estado de autenticacion, rol efectivo y vencimiento de sesion
  cuando aplique.
- **SolicitudLogin**: Representa el intento de autenticacion con email y contrasena
  enviados en cuerpo JSON.
- **RespuestaLogin**: Representa resultado de autenticacion (`ACCEPTED|DENIED`) y rol
  (`USER|ADMIN`) mediante contrato canonico de login en v4.
- **EntidadSidebar**: Representa cada entidad navegable disponible desde API para
  renderizado de menu lateral.
- **VistaTablaEntidad**: Representa datos tabulares renderizados dinamicamente para
  Empleados o Departamentos.
- **AccionToggleCrud**: Representa accion de usuario para agregar, editar, eliminar
  y, en Departamentos, ver empleados asociados.

When feature scope includes domain relationships, this section MUST explicitly
document cardinality, nullable assignment rules, and FK expectations.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El 100% de accesos no autenticados a rutas protegidas son redirigidos
  a login en una sola navegacion.
- **SC-002**: Al menos 95% de intentos de login validos completan acceso al dashboard
  en menos de 3 segundos en entorno de prueba definido.
- **SC-003**: El 100% de escenarios E2E obligatorios (login valido, login invalido,
  render empleados, render departamentos, CRUD basico, departamentos/{id}/empleados)
  pasan en pipeline de build.
- **SC-004**: El dashboard MUST mantener navegacion completa (sidebar, seleccion de
  entidad, tabla visible y accion de logout accesible) en viewport movil
  375x667 y escritorio 1366x768; el 100% de estos checks MUST pasar en Cypress
  sin overflow horizontal global (`body.scrollWidth == viewportWidth`).
- **SC-005**: El 100% de builds de release incluyen version frontend independiente
  visible en footer y trazable en artefacto de despliegue.
- **SC-006**: El 95% de renders iniciales de la tabla de entidad seleccionada
  completan en <= 2.5 segundos en entorno de prueba definido.
- **SC-007**: El 95% de renovaciones de sesion via `/api/v4/auth/refresh`
  completan en <= 500 ms en entorno de prueba definido.
