# Feature Specification: Gobierno inicial de API y Git

**Feature Branch**: `002-api-v1-paginacion-git`  
**Created**: 2026-03-04  
**Status**: Draft  
**Input**: User description: "define la versión inicial de la API, manejar paginación en endpoints de colecciones, definir remoto del repositorio y convención correcta de ramas"

## Clarifications

### Session 2026-03-04

- Q: ¿Qué convención de paginación global debe usar la API? → A: page inicia en 1, size por defecto 20, maxSize 100.
- Q: ¿Qué endpoints deben versionarse bajo `/api/v1`? → A: Todos los endpoints de negocio; endpoints técnicos fuera de versionado.
- Q: ¿Cómo manejar requests con `size` mayor al máximo permitido? → A: Rechazar con 400 Bad Request y mensaje claro de validación.
- Q: ¿Cuál es la rama oficial de integración y destino de PR? → A: `main`, con PR de feature branches hacia `main`.

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

### User Story 1 - Version inicial de API pública (Priority: P1)

Como consumidor de la API, necesito una versión pública inicial estable para integrar
mi cliente sin riesgo de ruptura inesperada.

**Why this priority**: Define el contrato base del producto y habilita integraciones
externas de forma controlada.

**Independent Test**: Se valida revisando que todos los endpoints públicos activos
estén publicados bajo una misma versión mayor inicial y con contrato documentado.

**Acceptance Scenarios**:

1. **Given** una API con endpoints públicos, **When** un cliente consulta rutas de
  negocio, **Then** todas las rutas comienzan con la versión mayor inicial definida.
2. **Given** un cambio compatible en la API, **When** se publica la actualización,
  **Then** la versión mayor inicial se mantiene sin cambios.

---

### User Story 2 - Paginación obligatoria en colecciones (Priority: P2)

Como consumidor de endpoints de listado, necesito respuestas paginadas para navegar
resultados en bloques predecibles y evitar cargas excesivas.

**Why this priority**: Reduce riesgos de rendimiento y estandariza la experiencia de
consumo para todos los listados.

**Independent Test**: Se valida llamando un endpoint de colección con y sin
parámetros de paginación y verificando límites y metadatos consistentes.

**Acceptance Scenarios**:

1. **Given** un endpoint que retorna una colección, **When** el cliente solicita una
  página específica, **Then** recibe únicamente el subconjunto solicitado y
  metadatos de paginación.
2. **Given** un endpoint que retorna una colección, **When** el cliente omite
  parámetros de paginación, **Then** se aplica una configuración por defecto
  documentada.

---

### User Story 3 - Gobernanza de repositorio y ramas (Priority: P3)

Como miembro del equipo, necesito un remoto oficial y una convención de ramas para
trabajar con trazabilidad y revisiones consistentes.

**Why this priority**: Asegura colaboración ordenada y reduce errores de integración.

**Independent Test**: Se valida comprobando el remoto configurado y que nuevas ramas
de trabajo sigan el patrón oficial de numeración y nombre corto.

**Acceptance Scenarios**:

1. **Given** un repositorio local, **When** se consulta el remoto `origin`,
  **Then** apunta a `https://github.com/Matador108117/dsw02-practica01.git`.
2. **Given** una nueva funcionalidad, **When** se crea su rama, **Then** cumple el
  patrón `NNN-short-name` y queda asociada a su carpeta en `specs/`.

---

### Edge Cases

<!--
  ACTION REQUIRED: The content in this section represents placeholders.
  Fill them out with the right edge cases.
-->

- Requests con `size > 100` DEBEN responder `400 Bad Request` con detalle de validación.
- ¿Qué ocurre si el cliente envía valores inválidos (negativos o no numéricos) en paginación?
- ¿Cómo se gestiona un cambio incompatible propuesto sobre la versión inicial de API?
- ¿Qué ocurre si el remoto `origin` ya existe con una URL distinta?

## Requirements *(mandatory)*

<!--
  ACTION REQUIRED: The content in this section represents placeholders.
  Fill them out with the right functional requirements.
-->

### Functional Requirements

- **FR-001**: El sistema DEBE definir una versión mayor inicial para todos los
  endpoints públicos de negocio bajo el prefijo `/api/v1`.
- **FR-002**: El sistema DEBE mantener la versión mayor inicial para cambios
  compatibles y registrar explícitamente cualquier cambio incompatible.
- **FR-009**: Los endpoints técnicos de plataforma (por ejemplo health, métricas,
  OpenAPI/Swagger) NO DEBEN quedar bajo `/api/v1`.
- **FR-003**: El sistema DEBE exigir paginación en cualquier endpoint que retorne
  colecciones.
- **FR-004**: El sistema DEBE exponer metadatos mínimos de paginación (página actual,
  tamaño solicitado, total de elementos o indicador equivalente de continuidad).
- **FR-005**: El sistema DEBE aplicar la convención global de paginación: `page`
  inicia en 1, `size` por defecto 20 y `maxSize` 100 por página.
- **FR-010**: El sistema DEBE rechazar requests con `size > 100` mediante
  `400 Bad Request` y mensaje de error explícito para corrección del cliente.
- **FR-006**: El repositorio DEBE tener configurado `origin` con la URL
  `https://github.com/Matador108117/dsw02-practica01.git`.
- **FR-007**: Las ramas de funcionalidad DEBEN seguir la convención `NNN-short-name`
  y estar vinculadas con su especificación de feature en `specs/`.
- **FR-008**: Cada pull request DEBE poder trazarse a requisitos y tareas de su
  feature mediante nombre de rama, spec y evidencia de validación.
- **FR-011**: La rama oficial de integración DEBE ser `main` y toda feature branch
  DEBE integrarse mediante pull request hacia `main`.

### Backend Constraints *(mandatory)*

- **BC-001**: Solution MUST run on Spring Boot 3 with Java 17.
- **BC-002**: Protected endpoints MUST define HTTP Basic Authentication behavior.
- **BC-003**: Data persistence MUST target PostgreSQL.
- **BC-004**: Local and CI database execution MUST be Docker-based.
- **BC-005**: API changes MUST include OpenAPI/Swagger documentation updates.
- **BC-006**: Spec MUST state required integration tests for auth, DB, and API contract.
- **BC-007**: Public API endpoints MUST be versioned with `/api/v{major}` and
  breaking changes MUST declare migration impact. En esta feature, el alcance aplica
  a endpoints de negocio, no a endpoints técnicos de framework/plataforma.
- **BC-008**: Collection endpoints MUST define pagination parameters plus default and
  maximum page limits. En esta feature: `page>=1`, `size` por defecto 20, `size<=100`.
- **BC-009**: Implementation workflow MUST define feature branch, PR traceability,
  expected commit granularity, and PR target branch (`main`).

### Assumptions & Dependencies

- Se asume que la versión inicial de API pública es `v1`.
- Se asume que todos los endpoints de lectura múltiple son considerados colecciones
  y por lo tanto requieren paginación.
- Se asume la convención de paginación global: `page` basado en 1, `size` default 20,
  `maxSize` 100.
- Se asume que existe un flujo de revisión por pull request para integrar cambios.
- Se asume que `main` está (o será) protegida para evitar pushes directos.
- Dependencia externa: disponibilidad y acceso al repositorio remoto en GitHub.

### Key Entities *(include if feature involves data)*

- **API Version Policy**: Regla de versionamiento para rutas públicas, incluyendo
  versión mayor activa y criterio de compatibilidad.
- **Pagination Policy**: Regla para parámetros, límites por defecto/máximo y
  metadatos de respuesta en endpoints de colección.
- **Repository Governance**: Regla de remoto oficial y convención de ramas para
  trazabilidad de cambios.

## Success Criteria *(mandatory)*

<!--
  ACTION REQUIRED: Define measurable success criteria.
  These must be technology-agnostic and measurable.
-->

### Measurable Outcomes

- **SC-001**: 100% de endpoints públicos de negocio quedan definidos bajo una única
  versión mayor inicial.
- **SC-002**: 100% de endpoints que retornan colecciones aceptan parámetros de
  paginación y responden con metadatos de navegación.
- **SC-003**: 100% de requests con parámetros de paginación inválidos reciben una
  respuesta de validación consistente y accionable (`400 Bad Request`).
- **SC-004**: 100% de nuevas features usan una rama con patrón `NNN-short-name`
  asociada a su carpeta en `specs/`.
- **SC-005**: 100% de clones nuevos del repositorio pueden verificar el remoto
  `origin` esperado sin configuración manual adicional.
