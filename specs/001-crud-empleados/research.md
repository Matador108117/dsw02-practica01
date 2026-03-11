# Research: CRUD de Empleados

## Decision 1: Arquitectura backend en Spring Boot 3 con capas controller-service-repository
- Decision: Usar arquitectura por capas (`controller`, `service`, `repository`, `model`) sobre Spring Boot 3 y Java 17.
- Rationale: Es el estándar constitucional del proyecto, simplifica mantenimiento y permite pruebas unitarias/integración por capa.
- Alternatives considered:
  - Arquitectura hexagonal completa: descartada para MVP por mayor complejidad inicial.
  - Endpoint + acceso directo a DB sin servicio: descartado por acoplamiento y menor testabilidad.

## Decision 2: Persistencia PostgreSQL con PK natural `clave` (INTEGER)
- Decision: Modelar `empleado.clave` como `INTEGER` PK natural, no autogenerada; `nombre`, `direccion`, `telefono` como `VARCHAR(100)` `NOT NULL`.
- Rationale: Cumple los FR-002/FR-002a/FR-002b/FR-003/FR-003a y mantiene reglas de negocio en modelo y base de datos.
- Alternatives considered:
  - PK surrogate autoincremental: rechazada porque contradice requerimiento explícito de captura de `clave`.
  - Campos de texto más largos sin restricción DB: rechazada por riesgo de inconsistencia con validación API.

## Decision 3: Validación dual (Bean Validation + restricciones SQL)
- Decision: Aplicar validaciones en entrada API (`@NotBlank`, `@Size(max=100)`, `@NotNull`) y reforzarlas con constraints SQL (`NOT NULL`, `VARCHAR(100)`, PK única).
- Rationale: Defensa en profundidad; evita datos inválidos tanto en capa HTTP como en persistencia.
- Alternatives considered:
  - Sólo validación en controlador: rechazada por no proteger escritura fuera de API.
  - Sólo validación en DB: rechazada por mala UX y mensajes de error tardíos.

## Decision 4: Seguridad con HTTP Basic Auth en todos los endpoints CRUD
- Decision: Exigir autenticación HTTP Basic para `POST`, `GET`, `PUT` y `DELETE` de empleados.
- Rationale: Requisito constitucional y FR-010; implementación simple y verificable para alcance MVP.
- Alternatives considered:
  - Endpoints públicos en lectura: rechazado por conflicto con requisito de operaciones protegidas.
  - JWT/OAuth2: rechazado por complejidad innecesaria para práctica.

## Decision 5: Contrato OpenAPI 3.0 como fuente de integración
- Decision: Definir contrato en `contracts/empleados.openapi.yaml` con esquemas, seguridad y respuestas de validación/no encontrado.
- Rationale: Cumple FR-011 y permite prueba de contrato y documentación Swagger trazable.
- Alternatives considered:
  - Documentación sólo en README: rechazada por falta de contrato formal.
  - Generar contrato después del código: rechazada; se prioriza diseño contract-first en planeación.

## Decision 6: Estrategia de pruebas de integración enfocada en riesgos constitucionales
- Decision: Planear pruebas de integración para autenticación (401/200), persistencia CRUD contra PostgreSQL en contenedor y cumplimiento de contrato (status/payload).
- Rationale: Cumple BC-006 y principio V de la constitución.
- Alternatives considered:
  - Sólo pruebas unitarias: rechazadas por no validar seguridad ni integración DB real.
  - Pruebas manuales ad hoc: rechazadas por baja repetibilidad en CI.