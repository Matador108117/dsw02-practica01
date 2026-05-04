# Data Model: Empleados con Clave Compuesta

## Entity: ClaveEmpleado

### Fields
- `prefijo` (string, required, fixed value `EMP-`)
- `consecutivo` (integer, required, auto-generated, incremental, positive)
- `clave` (string derived for API/view: `EMP-` + consecutivo padded a 6 dígitos)

### Validation Rules
- `prefijo` MUST ser exactamente `EMP-`.
- `consecutivo` MUST ser entero positivo.
- `(prefijo, consecutivo)` MUST ser único y actuar como PK compuesta.

## Entity: Empleado

### Fields
- `claveEmpleado` (composite key, required)
- `nombre` (string, required, not blank, max 100)
- `direccion` (string, required, not blank, max 100)
- `telefono` (string, required, not blank, max 100)
- `created_at` (timestamp, optional)
- `updated_at` (timestamp, optional)

### Validation Rules
- `nombre`, `direccion`, `telefono` MUST ser obligatorios y no vacíos.
- `nombre`, `direccion`, `telefono` MUST tener longitud máxima 100.
- `claveEmpleado` MUST generarse en alta y MUST NOT modificarse en update.

## Entity: EventoEmpleado

### Fields
- `id` (uuid/integer, required)
- `clave` (string, required)
- `tipo_evento` (enum: ALTA, ACTUALIZACION, ELIMINACION)
- `fecha_hora` (timestamp, required)
- `detalle` (string, optional)

## Relationships
- `Empleado` 1..1 `ClaveEmpleado` (composición).
- `Empleado` 1..N `EventoEmpleado`.

## State Transitions
- `NoExiste` -> `Activo`: alta válida con clave compuesta generada.
- `Activo` -> `Activo`: actualización válida de campos de negocio (clave inmutable).
- `Activo` -> `Eliminado`: eliminación lógica/física según implementación.
- `NoExiste` -> `NoExiste`: consulta/update/delete por clave inexistente retorna `404`.

## Database Mapping (PostgreSQL)

Tabla sugerida: `empleado`

- `prefijo VARCHAR(4) NOT NULL DEFAULT 'EMP-'`
- `consecutivo BIGINT NOT NULL`
- `nombre VARCHAR(100) NOT NULL`
- `direccion VARCHAR(100) NOT NULL`
- `telefono VARCHAR(100) NOT NULL`
- `created_at TIMESTAMP NULL`
- `updated_at TIMESTAMP NULL`
- `PRIMARY KEY (prefijo, consecutivo)`

Índices/constraints recomendados:
- `UNIQUE (prefijo, consecutivo)` (implícito por PK)
- `CHECK (prefijo = 'EMP-')`
- `CHECK (char_length(trim(nombre)) > 0)`
- `CHECK (char_length(trim(direccion)) > 0)`
- `CHECK (char_length(trim(telefono)) > 0)`

Generación de consecutivo:
- Secuencia dedicada (`empleado_consecutivo_seq`) o mecanismo equivalente transaccional.