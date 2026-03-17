# Data Model: Gestion de credenciales y roles de empleado

## Entity: Empleado
- Purpose: Representa identidad de acceso y datos de negocio del empleado.
- Primary Key: `prefijo` + `consecutivo` (clave compuesta existente).

### Fields
- `prefijo` (string, required, fixed `EMP-`)
- `consecutivo` (long, required, > 0)
- `nombre` (string, required, max 100)
- `direccion` (string, required, max 100)
- `telefono` (string, required, max 100)
- `correo_electronico` (string, required, unique case-insensitive, max 150, email valido)
- `contrasena_hash` (string, required, max 255)
- `rol` (enum, required: `ADMIN`, `USER`)
- `activo` (boolean, required, default true)
- `created_at` (timestamp)
- `updated_at` (timestamp)

### Validation Rules
- `correo_electronico` debe normalizarse para comparacion case-insensitive antes de persistir/consultar.
- `correo_electronico` no debe repetirse bajo comparacion case-insensitive.
- `contrasena` de entrada no se persiste; solo se persiste `contrasena_hash`.
- `contrasena` es transitoria de entrada (registro/autenticacion) y MUST NOT
	almacenarse en texto plano.
- La autenticacion en metodos protegidos usa HTTP Basic (`type=http`,
	`scheme=basic`) con `correo_electronico` como username.
- La validacion de Basic Auth debe resolver `correo_electronico` en tabla
	`empleado` y comparar hash derivado de `contrasena` transitoria contra
	`contrasena_hash` persistido.
- `rol` debe ser uno de los valores del enum.
- Campos textuales requeridos no aceptan blancos.

### State Transitions
- `activo: true -> false`: deshabilitacion de acceso.
- `rol: USER <-> ADMIN`: cambio administrativo con efecto inmediato.
- `contrasena_hash`: se reemplaza en eventos de cambio de contrasena.

## Entity: AuthAttempt
- Purpose: Registrar intentos de autenticacion para limite y bloqueo temporal por correo+IP.
- Key: `correo_electronico` + `ip_origen`.

### Fields
- `correo_electronico` (string, required)
- `ip_origen` (string, required)
- `failed_count_window` (int, required)
- `window_start_at` (timestamp, required)
- `blocked_until` (timestamp, nullable)
- `updated_at` (timestamp, required)

### Validation Rules
- `failed_count_window` reinicia al cambiar ventana (> 15 minutos).
- Si `blocked_until > now`, se rechaza autenticacion.
- Respuesta esperada ante bloqueo activo: `423`.
- Respuesta esperada ante credenciales invalidas sin bloqueo: `401`.

## Entity: ApiVersionSupportPolicy
- Purpose: Registrar y aplicar en runtime la politica operacional de convivencia de
	versiones v1/v2 con enforcement de sunset.
- Key: `api_name`.

### Fields
- `api_name` (string, required, unique)
- `deprecated_version` (string, required, ejemplo `v1`)
- `active_version` (string, required, ejemplo `v2`)
- `deprecation_notice` (string, required)
- `release_v2_at_utc` (timestamp UTC, required, immutable)
- `sunset_at_utc` (timestamp UTC, required)

### Validation Rules
- `active_version` debe ser `v2` para este feature.
- `deprecated_version` debe ser `v1`.
- `sunset_at_utc` debe fijarse a `release_v2_at_utc + 90 dias naturales`.
- Si `now_utc >= sunset_at_utc`, cualquier solicitud a `v1` debe responder `410`.
- No se permite bypass manual del enforcement de sunset.

### Relationships
- Empleado (1) -> (N) AuthAttempt por `correo_electronico`.
- ApiVersionSupportPolicy es consumida por componentes de routing/versioning para
	validar soporte de `v1` en runtime.
