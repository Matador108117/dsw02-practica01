# Data Model: CRUD de Empleados

## Entity: Empleado

### Fields
- `clave` (integer, required, primary key, natural key, not auto-generated)
- `nombre` (string, required, non-empty, max length 100)
- `direccion` (string, required, non-empty, max length 100)
- `telefono` (string, required, non-empty, max length 100)
- `created_at` (timestamp, optional for operability/log correlation)
- `updated_at` (timestamp, optional for operability/log correlation)

> Nota: `created_at` y `updated_at` son opcionales desde requerimientos de negocio,
> pero recomendados para trazabilidad operativa junto con FR-012.

## Relationships
- No hay relaciones obligatorias con otras entidades en este alcance MVP.

## Validation Rules
- `clave` MUST ser entera (sin decimales).
- `clave` MUST ser única en toda la colección.
- `clave` MUST proveerse por el usuario en alta.
- `nombre`, `direccion`, `telefono` MUST ser obligatorios y no vacíos.
- `nombre`, `direccion`, `telefono` MUST tener longitud máxima de 100 caracteres.

## State Transitions
- `NoExiste` -> `Activo`: al crear un empleado válido con `clave` no registrada.
- `Activo` -> `Activo`: al actualizar campos permitidos (`nombre`, `direccion`, `telefono`) con datos válidos.
- `Activo` -> `Eliminado`: al eliminar un empleado existente.
- `NoExiste` -> `NoExiste`: intentos de consulta/actualización/eliminación sobre `clave` inexistente retornan `404`.

## Database Mapping (PostgreSQL)

Tabla sugerida: `empleado`

- `clave INTEGER PRIMARY KEY`
- `nombre VARCHAR(100) NOT NULL`
- `direccion VARCHAR(100) NOT NULL`
- `telefono VARCHAR(100) NOT NULL`
- `created_at TIMESTAMP NULL`
- `updated_at TIMESTAMP NULL`

Índices:
- PK en `clave` (implícito)

Checks opcionales de robustez:
- `CHECK (char_length(trim(nombre)) > 0)`
- `CHECK (char_length(trim(direccion)) > 0)`
- `CHECK (char_length(trim(telefono)) > 0)`