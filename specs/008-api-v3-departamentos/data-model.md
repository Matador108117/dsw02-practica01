# Data Model: API v3 Departamentos y Relacion Empleados

## Entity: Departamento
- Purpose: Unidad organizacional a la que se pueden asociar empleados.
- Primary Key: `id` (string, formato `DEP-<numero_secuencial_de_6_digitos>` con padding fijo exacto, ejemplo `DEP-000001`).

### Fields
- `id` (string, required, PK, pattern `^DEP-[0-9]{6}$`)
- `nombre` (string, required, max 150, non-blank)
- `created_at` (timestamp, optional auditing)
- `updated_at` (timestamp, optional auditing)

### Validation Rules
- `nombre` no puede ser nulo, vacio o solo espacios.
- `id` debe seguir formato canonico con prefijo `DEP-` y consecutivo con padding.

### State Transitions
- `nombre`: actualizable via PUT.
- Eliminacion: permitida solo cuando no existan empleados asociados.

## Entity: Empleado (v3 projection)
- Purpose: Recurso existente de empleados extendido con asociacion opcional a departamento.
- Primary Key: clave compuesta existente del dominio de empleados (`prefijo` + `consecutivo`).

### Fields (scope v3)
- `prefijo` (string, required)
- `consecutivo` (long, required)
- `nombre` (string, required)
- `direccion` (string, required)
- `telefono` (string, required)
- `correo_electronico` (string, required)
- `contrasena_hash` (string, required, persistido)
- `departamento_id` (string, nullable, FK a `departamentos.id`, mapeado desde payload API `departamentoId`)

### Validation Rules
- En POST/PUT de empleados, `departamento_id` puede omitirse o enviarse como `null`.
- El campo de payload `departamentoId` se mapea al campo persistido `departamento_id`.
- Si `departamento_id` se informa, debe existir previamente un `Departamento` persistido.
- Si `departamento_id` no existe, la API responde `422 Unprocessable Entity`.
- `contrasena` se mantiene como input-only; no se persiste en texto plano.

### State Transitions
- `departamento_id: null -> DEP-xxxxxx` (asignacion)
- `departamento_id: DEP-xxxxxx -> DEP-yyyyyy` (reasignacion)
- `departamento_id: DEP-xxxxxx -> null` (desasignacion)

## Relationship: Departamento (1) -> (N) Empleado
- Cardinality: un `Departamento` puede tener cero o muchos `Empleado`.
- Cardinality inverse: un `Empleado` pertenece a cero o un `Departamento`.
- Constraint: FK explicita `empleados.departamento_id` referencia `departamentos.id`.
- Delete rule: si existen empleados asociados, eliminar departamento debe fallar con `409 Conflict`.

## Migration Model
- Nueva tabla `departamentos`:
  - `id` varchar PK.
  - `nombre` varchar(150) NOT NULL.
- Alter tabla `empleados`:
  - agregar columna `departamento_id` varchar NULL.
  - agregar FK explicita a `departamentos.id`.
- Backfill:
  - registros existentes en `empleados` quedan con `departamento_id = NULL` cuando no existe regla historica de asignacion.
