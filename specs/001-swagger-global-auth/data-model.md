# Data Model: Correccion de acceso global en Swagger UI

## Entity: SwaggerAuthSession (conceptual)
- Purpose: Representa el estado de autenticacion que Swagger UI reutiliza para invocar operaciones protegidas sin relogin por metodo.
- Persistence: No persistente en base de datos; estado de sesion del cliente Swagger.

### Fields
- `auth_scheme` (enum, required): `basic`
- `principal` (string, required): corresponde a `correo_electronico`
- `is_authenticated` (boolean, required)
- `last_backend_status` (int, required): ultimo codigo de respuesta observado en endpoint protegido
- `reauthorize_required` (boolean, required)

### Validation Rules
- `is_authenticated` solo es verdadero tras Authorize exitoso.
- Si `last_backend_status = 401`, entonces `reauthorize_required = true`.
- Cuando `reauthorize_required = true`, ninguna operacion protegida debe ejecutarse hasta nuevo Authorize.

### State Transitions
- `NotAuthenticated -> Authenticated`: Authorize valido.
- `Authenticated -> ReauthorizeRequired`: primer `401` recibido desde backend.
- `ReauthorizeRequired -> Authenticated`: nuevo Authorize valido.

## Entity: TestEmployeeBootstrapPolicy (conceptual)
- Purpose: Regla de inicializacion idempotente para garantizar usuario de prueba obligatorio.
- Persistence: Se aplica sobre entidad `Empleado` existente.

### Required Target Values
- `nombre = prueba2`
- `correo_electronico = emailprueba@gmail.com`
- `rol = USER`
- `contrasena_hash` derivada de `contra123`

### Validation Rules
- Si no existe empleado con ese email, debe crearse.
- Si existe, debe normalizarse a valores requeridos sin duplicar registro.
- `contrasena` en texto plano nunca se persiste.
- El bootstrap no se expone por endpoint en runtime.

## Entity: Empleado (existing persisted model impacted)
- Purpose: Identidad persistida usada para autenticacion/autorizacion API.
- Persistence: Tabla `empleado` en PostgreSQL.

### Impacted Attributes
- `correo_electronico` (required, unique case-insensitive)
- `contrasena_hash` (required)
- `rol` (`USER` o `ADMIN`)

### Relationship Notes
- `SwaggerAuthSession.principal` debe mapear a `Empleado.correo_electronico`.
- La politica de bootstrap opera sobre un unico `Empleado` de prueba por email canonico.
