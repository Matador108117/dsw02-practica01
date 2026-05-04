# Research: API v3 Departamentos y Relacion Empleados

## Decision 1: Versionado oficial del contrato
- Decision: Declarar oficialmente esta evolucion bajo `/api/v3`.
- Rationale: Se introduce nueva entidad persistente, nuevas rutas publicas y cambios de esquema relacional, lo cual constituye cambio mayor.
- Alternatives considered:
  - Mantener `/api/v2`: descartado por incumplimiento de regla constitucional de major bump.
  - Publicar rutas mixtas v2/v3 para el mismo recurso nuevo: descartado por complejidad y ambiguedad contractual.

## Decision 2: Integridad relacional Departamento-Empleado
- Decision: Usar FK explicita `empleados.departamento_id -> departamentos.id` con `departamento_id` nullable.
- Rationale: Asegura consistencia de datos sin impedir altas de empleados no asignados.
- Alternatives considered:
  - Relacion implicita sin FK: descartado por incumplimiento constitucional.
  - `departamento_id` no nullable: descartado porque el negocio permite creacion sin asignacion.

## Decision 3: Politica de eliminacion de departamentos con dependencias
- Decision: Rechazar eliminacion cuando existan empleados asociados y responder `409 Conflict`.
- Rationale: Evita perdida accidental de asociaciones y mantiene reglas explicitas del dominio.
- Alternatives considered:
  - Nullificar automatico: descartado por efectos laterales implicitos.
  - Cascada de empleados: descartado por riesgo de perdida de datos.

## Decision 4: Semantica de error para referencia inexistente en empleados
- Decision: Cuando `departamento_id` no exista en POST/PUT de empleados, responder `422 Unprocessable Entity`.
- Rationale: La solicitud es sintacticamente valida pero semanticamente invalida por referencia de negocio inexistente.
- Alternatives considered:
  - `400 Bad Request`: menos preciso para error de referencia de dominio.
  - `404 Not Found`: ambiguo para operaciones de creacion/actualizacion de otro recurso.

## Decision 5: Paginacion de colecciones v3
- Decision: Estandarizar `page` y `size` con default `page=0`, `size=25` y `maxSize=100`.
- Rationale: Cumple constitucion y controla costo de consultas.
- Alternatives considered:
  - Sin paginacion: descartado por incumplimiento constitucional.
  - Limites mayores por defecto: descartado por impacto de rendimiento y transferencia.

## Decision 6: Formato de identificador de departamentos
- Decision: Definir `departamentos.id` como `DEP-<numero_secuencial>` con padding fijo (ej. `DEP-000001`).
- Rationale: Mejora trazabilidad operativa y legibilidad en API/soporte.
- Alternatives considered:
  - UUID puro: descartado por baja ergonomia operativa en este contexto.
  - Prefijo alterno (`DPT`): descartado por consistencia con decision de negocio.

## Decision 7: Estrategia de migracion y backfill
- Decision: Crear migracion Flyway incremental para `departamentos`, `empleados.departamento_id`, FK explicita y backfill a `NULL` para registros existentes sin regla de asignacion historica.
- Rationale: Evita reescribir historico de migraciones y mantiene despliegues idempotentes.
- Alternatives considered:
  - Modificar migraciones anteriores: descartado por anti-patron en Flyway.
  - Asignacion masiva a departamento por defecto: descartado por no existir regla de negocio validada.

## Decision 8: Objetivos de rendimiento y alcance
- Decision: Definir objetivos iniciales P95 <= 800 ms para listados de departamentos y P95 <= 1000 ms para endpoint relacional, con paginacion obligatoria.
- Rationale: Proporciona umbrales verificables para pruebas de integracion/operacion sin sobredimensionar el alcance.
- Alternatives considered:
  - Sin objetivos medibles: descartado por falta de criterio de aceptacion operativa.
  - Objetivos mas estrictos sin baseline: descartado por riesgo de metas no realistas para el entorno actual.
