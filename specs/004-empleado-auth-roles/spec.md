# Feature Specification: Gestion de credenciales y roles de empleado

**Feature Branch**: `004-empleado-auth-roles`  
**Created**: 2026-03-12  
**Status**: Draft  
**Input**: User description: "define una especificacion donde se desea modificar la tabla de empleado agregando correo electronico y constrasena asi como considerar los roles de empleado: admin el crud completo y user solo consultas, asi como la exigencias de autentificacion con los mismos datos"

## Clarifications

### Session 2026-03-12

- Q: Como debe almacenarse la contrasena del empleado? -> A: Hash irreversible (Argon2id/Bcrypt)
- Q: Que estrategia de migracion de rol se aplica a empleados existentes? -> A: Empleados existentes como `USER` y `ADMIN` bootstrap controlado
- Q: Que politica de limite de intentos fallidos de autenticacion se aplicara? -> A: 5 intentos en 15 minutos por correo+IP, bloqueo temporal de 15 minutos
- Q: Como evoluciona el versionado de rutas API durante la transicion? -> A: Soporte temporal de `v1`, introduccion de `v2` y declaracion de `v1` como deprecada
- Q: En cuanto tiempo debe retirarse `v1` despues de liberar `v2`? -> A: Retiro en 90 dias calendario
- Q: Que codigos HTTP deben usarse para credenciales invalidas y bloqueo temporal? -> A: `401` para credenciales invalidas y `423` para bloqueo temporal
- Q: La unicidad de correo debe considerar mayusculas y minusculas? -> A: Unicidad insensible a mayusculas/minusculas

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Autenticacion con correo y contrasena (Priority: P1)

Como empleado del sistema, quiero autenticarme con correo electronico y contrasena
para acceder a la API con una identidad valida.

**Why this priority**: Sin autenticacion no existe control de acceso ni trazabilidad
de operaciones.

**Independent Test**: Puede probarse ejecutando intentos de inicio de sesion validos
e invalidos y verificando acceso permitido o denegado en endpoints protegidos.

**Acceptance Scenarios**:

1. **Given** un empleado con correo y `contrasena_hash` registrados en tabla
  `empleado`, **When** envia credenciales HTTP Basic validas (`username=correo`,
  `password=contrasena` transitoria), **Then** el sistema valida por comparacion de
  hash y otorga acceso a endpoints permitidos por su rol.
2. **Given** un intento con correo inexistente o contrasena incorrecta, **When** se
  solicita acceso a la API, **Then** la solicitud es rechazada.
3. **Given** un empleado sin datos de correo o contrasena, **When** se intenta crear
  o actualizar el registro, **Then** la operacion es rechazada por datos obligatorios.

---

### User Story 2 - Rol admin con CRUD completo (Priority: P2)

Como usuario con rol admin, quiero crear, consultar, actualizar y eliminar empleados
para administrar el catalogo completo.

**Why this priority**: El control operativo del negocio depende de la capacidad de
administrar datos de empleados.

**Independent Test**: Puede probarse autenticando un admin y ejecutando operaciones
de alta, consulta, modificacion y baja sobre empleados.

**Acceptance Scenarios**:

1. **Given** un admin autenticado, **When** solicita crear un empleado con correo y
  contrasena validos, **Then** el registro se crea correctamente.
2. **Given** un admin autenticado, **When** solicita actualizar o eliminar un empleado,
  **Then** la API ejecuta la operacion y refleja el estado esperado.

---

### User Story 3 - Rol user solo consultas (Priority: P3)

Como usuario con rol user, quiero consultar informacion de empleados sin poder
modificar registros para cumplir una politica de solo lectura.

**Why this priority**: Minimiza riesgo operativo al separar lectura de acciones de
alto impacto.

**Independent Test**: Puede probarse autenticando un user y verificando que puede
consultar pero no crear, actualizar ni eliminar empleados.

**Acceptance Scenarios**:

1. **Given** un user autenticado, **When** solicita listar o consultar empleados,
   **Then** obtiene respuesta exitosa.
2. **Given** un user autenticado, **When** intenta crear, actualizar o eliminar,
   **Then** la API rechaza la operacion por permisos insuficientes.

---

### Edge Cases

- Correo electronico con formato invalido debe ser rechazado.
- Username de Basic Auth que no sea correo electronico valido debe rechazarse.
- Solicitudes sin cabecera Authorization Basic o con esquema distinto de Basic
  deben rechazarse.
- Correo electronico duplicado entre empleados debe rechazarse para evitar ambiguedad
  de autenticacion.
- Variantes de correo con distinto uso de mayusculas/minusculas deben tratarse como el
  mismo valor para unicidad y autenticacion.
- Contrasena vacia o solo espacios debe ser rechazada.
- Intento de autenticar usuario deshabilitado o inexistente debe ser rechazado.
- Un user autenticado intentando operaciones de escritura no debe alterar datos.
- Cambios de rol deben aplicarse inmediatamente en el siguiente intento de acceso.
- Al superar 5 intentos fallidos en 15 minutos por correo+IP, el acceso debe quedar
  temporalmente bloqueado durante 15 minutos.
- Credenciales invalidas deben responder `401`, mientras que bloqueo temporal por
  exceso de intentos debe responder `423`.
- Durante la convivencia de versiones, solicitudes a `v1` deben incluir aviso claro de
  deprecacion en documentacion y comportamiento observable.
- Al cumplirse el periodo de 90 dias calendario desde la liberacion de `v2`, las
  solicitudes a `v1` deben dejar de estar disponibles.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema MUST requerir `correo_electronico` y `contrasena` como
  datos obligatorios de entrada para registro y autenticacion de empleados.
- **FR-002**: El sistema MUST rechazar altas y actualizaciones de empleado cuando
  falte correo electronico o contrasena.
- **FR-003**: El sistema MUST autenticar acceso a endpoints protegidos mediante
  HTTP Basic Authentication (`type=http`, `scheme=basic`), usando
  `correo_electronico` como username y `contrasena` como credencial transitoria.
- **FR-003a**: El sistema MUST resolver la identidad de autenticacion buscando el
  `correo_electronico` en la tabla `empleado` y MUST validar la `contrasena`
  transitoria por comparacion de hash derivado contra `contrasena_hash`
  almacenado.
- **FR-004**: El sistema MUST asignar a cada empleado un rol de acceso (`ADMIN` o
  `USER`) para evaluacion de permisos.
- **FR-005**: El sistema MUST permitir a `ADMIN` ejecutar operaciones completas de
  crear, consultar, actualizar y eliminar empleados.
- **FR-006**: El sistema MUST permitir a `USER` solo operaciones de consulta sobre
  empleados.
- **FR-007**: El sistema MUST denegar operaciones de escritura solicitadas por
  usuarios con rol `USER`.
- **FR-008**: El sistema MUST validar formato de correo electronico antes de aceptar
  credenciales.
- **FR-009**: El sistema MUST evitar la coexistencia de dos empleados con el mismo
  correo electronico, usando comparacion insensible a mayusculas/minusculas.
- **FR-010**: El sistema MUST registrar eventos de autenticacion exitosa,
  autenticacion fallida y autorizacion denegada.
- **FR-010a**: Cada evento de FR-010 MUST incluir `timestamp_utc`, `userId`
  (o identificador equivalente), `resultado` y `origen_solicitud`.
- **FR-011**: El sistema MUST tratar `contrasena` como dato transitorio de entrada,
  persistir unicamente `contrasena_hash` y validar autenticacion por comparacion de
  hash en registros de `empleado`, sin exponer contrasena en texto plano.
- **FR-012**: Durante migracion, el sistema MUST asignar rol `USER` por defecto a
  empleados existentes mediante backfill idempotente y MUST crear un `ADMIN`
  bootstrap controlado para operaciones CRUD administrativas.
- **FR-012a**: La migracion de backfill de rol MUST tener prueba de verificacion
  posterior que garantice que ningun registro queda sin rol.
- **FR-013**: El sistema MUST aplicar limite de autenticacion por correo+IP: maximo
  5 intentos fallidos en 15 minutos y bloqueo temporal de 15 minutos al superar
  el umbral.
- **FR-014**: El sistema MUST soportar temporalmente endpoints de empleados en `v1`
  y en `v2` durante transicion controlada.
- **FR-015**: El sistema MUST declarar `v1` como deprecada en contrato API y
  documentacion oficial cuando `v2` entre en operacion.
- **FR-016**: El sistema MUST calcular `sunset_at_utc` como `release_v2_at_utc`
  mas 90 dias naturales, usando UTC como unica referencia temporal.
- **FR-017**: El sistema MUST responder `401` ante credenciales invalidas y MUST
  responder `423` cuando la cuenta/identidad se encuentre temporalmente bloqueada
  por politica de intentos fallidos.
- **FR-018**: El sistema MUST registrar la fecha oficial de release de `v2` como
  timestamp inmutable en UTC y usarla como unico origen del computo de sunset.
- **FR-019**: Durante el plazo de deprecacion de 90 dias naturales, `v1` MUST
  permanecer disponible; al vencer dicho plazo, toda solicitud a endpoints `v1`
  MUST responder `410 Gone`.
- **FR-020**: El sistema MUST impedir bypass manual de la regla de retiro de `v1`
  una vez alcanzado el sunset.
- **FR-021**: El bootstrap `ADMIN` MUST leer secreto inicial desde variable segura,
  permitir unicamente un `ADMIN` bootstrap, forzar cambio de contrasena en primer
  login y registrar evento de creacion.
- **FR-022**: Durante deprecacion, los endpoints `v1` MUST exponer headers
  `Deprecation: true` y `Sunset: <timestamp UTC>`, validados por prueba
  contractual automatizada.

### Backend Constraints *(mandatory)*

- **BC-001**: Solution MUST run on Spring Boot 3 with Java 17.
- **BC-002**: Protected endpoints MUST define HTTP Basic Authentication
  (`type=http`, `scheme=basic`) behavior.
- **BC-002a**: El username de Basic Auth MUST mapearse a `correo_electronico`
  persistido en la tabla `empleado`.
- **BC-002b**: La `contrasena` de Basic Auth MUST tratarse como transitoria y
  validarse por comparacion de hash derivado contra `contrasena_hash` persistido.
- **BC-003**: Authorization MUST enforce `USER` read-only access and `ADMIN` full CRUD access.
- **BC-004**: Data persistence MUST target PostgreSQL.
- **BC-005**: Local and CI database execution MUST be Docker-based.
- **BC-006**: API changes MUST include OpenAPI/Swagger documentation updates.
- **BC-007**: Spec MUST state required integration tests for auth, role authorization,
  DB, and API contract.
- **BC-008**: Public API endpoints MUST be versioned with `/api/v{major}` and
  breaking changes MUST declare migration impact.
- **BC-009**: Collection endpoints MUST define pagination parameters plus default and
  maximum page limits.
- **BC-010**: Implementation workflow MUST usar ramas con patron:
  `feature/<id>-<short-name>`, `fix/<id>-<short-name>` o `chore/<scope>`;
  MUST exigir trazabilidad tarea->branch->PR y granularidad de commits atomica.
- **BC-011**: La persistencia de credenciales MUST almacenar exclusivamente
  `contrasena_hash` (hash irreversible) como representacion canonica; `contrasena`
  es dato transitorio de entrada y MUST NOT persistirse en texto plano.
- **BC-012**: La estrategia de versionado MUST introducir `v2` manteniendo soporte
  temporal de `v1` con estado deprecado.
- **BC-013**: La deprecacion de `v1` MUST incluir fecha de retiro y ventana exacta
  de 90 dias naturales desde `release_v2_at_utc`.
- **BC-014**: El contrato API MUST distinguir semanticamente errores de autenticacion
  (`401`) de estados de bloqueo temporal (`423`).
- **BC-015**: La regla de unicidad de correo MUST aplicarse con normalizacion
  insensible a mayusculas/minusculas.
- **BC-016**: El enforcement del sunset de `v1` MUST ejecutarse automaticamente
  basado en fecha efectiva y reloj UTC.
- **BC-017**: El sistema MUST responder `410 Gone` para solicitudes `v1` posteriores
  al sunset sin excepciones por configuracion manual.
- **BC-018**: La entidad `ApiVersionSupportPolicy` es obligatoria y MUST existir con
  migracion de base de datos, repositorio y uso activo en runtime para validar
  soporte de version.
- **BC-019**: Los objetivos de rendimiento MUST validarse mediante pruebas de carga
  automatizadas en entorno controlado y evidenciarse en CI.
- **BC-020**: La PR MUST incluir checklist obligatorio de atomicidad de commits,
  no mezcla de concerns y evidencia de pruebas, con referencia explicita al ID de
  tarea.
- **BC-021**: Las migraciones MUST usar el siguiente numero libre, MUST NOT
  sobrescribir migraciones historicas, MUST NOT reutilizar numeros y MUST evitar
  deriva de esquema entre entornos.
- **BC-022**: La transicion `contrasena` legacy -> `contrasena_hash` MUST ejecutarse
  con migracion incremental, verificando integridad post-migracion y eliminando
  persistencia de texto plano.
- **BC-023**: El cierre de feature MUST declararse explicitamente mediante commit
  etiquetado, acta tecnica breve y aprobacion de comite tecnico.
- **BC-024**: Una nueva version mayor SHALL iniciarse solo por decision explicita del
  comite tecnico cuando exista cambio de contrato publico.

### Key Entities *(include if feature involves data)*

- **Empleado**: Representa a la persona usuaria de la API de empleados. Atributos
  clave: identificador de empleado, correo electronico, contrasena_hash, rol de
  acceso, estado de vigencia del registro.
- **Rol de Acceso**: Clasificacion funcional del empleado para control de permisos.
  Valores relevantes: `ADMIN` (CRUD completo) y `USER` (solo consultas).
- **Credencial de Acceso**: Combinacion de correo electronico y contrasena usada para
  autenticar solicitudes en endpoints protegidos; la contrasena se persiste como hash
  irreversible.
- **ApiVersionSupportPolicy**: Politica persistida de soporte de versiones con
  `release_v2_at_utc` inmutable y `sunset_at_utc` para enforcement automatico de
  disponibilidad de `v1`.

## Assumptions

- El sistema mantiene la semantica actual de endpoints y aplica reglas de acceso por
  rol sin ampliar alcance a nuevos modulos fuera de empleados.
- La transicion de versiones contempla convivencia temporal `v1` y `v2`, con `v1`
  en estado deprecado.
- Las consultas permitidas para `USER` incluyen lectura individual y lectura de listas.
- La migracion de empleados existentes asigna rol `USER` por defecto y contempla un
  `ADMIN` bootstrap controlado para habilitar operacion administrativa inicial.
- Las politicas detalladas de rotacion/recuperacion de contrasena quedan fuera de este
  alcance y se trataran en una especificacion posterior.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% de los registros nuevos de empleado se crean con correo electronico
  y contrasena no vacios.
- **SC-002**: 100% de intentos de escritura por usuarios con rol `USER` son
  rechazados sin modificar datos.
- **SC-003**: Al menos 95% de intentos de autenticacion con credenciales validas
  completan acceso en menos de 2000 ms (P95).
- **SC-004**: 100% de endpoints de empleados exponen comportamiento consistente con la
  matriz de permisos (`ADMIN` CRUD, `USER` solo consulta) en pruebas de aceptacion.
- **SC-005**: Al menos 95% de solicitudes de listados de empleados completan en
  menos de 800 ms (P95) bajo entorno controlado.
- **SC-006**: 100% de ejecuciones de CI publican evidencia de pruebas de carga y
  bloquean promocion cuando no se cumplen umbrales de rendimiento.

## Normative Clarifications

### 10.1 Gestion de Credenciales

- Referencia canonica: FR-003, FR-003a, FR-010, FR-010a, FR-011, FR-021,
  BC-002, BC-002a, BC-002b, BC-011.

### 10.2 Politica de Sunset de Versiones

- Referencia canonica: FR-014, FR-015, FR-016, FR-018, FR-019, FR-020, FR-022,
  BC-012, BC-013, BC-016, BC-017.

### 10.3 Entidad ApiVersionSupportPolicy

- Referencia canonica: BC-018.

### 10.4 Objetivos de Rendimiento y Validacion

- Referencia canonica: SC-003, SC-005, SC-006, BC-019.

### 10.5 Regla Temporal Oficial

- Referencia canonica: FR-016, FR-018, FR-019, BC-016, BC-017.

### 10.6 Separacion de Responsabilidades Normativas

- FR define comportamiento observable de negocio.
- BC define restricciones tecnicas de implementacion.
- Se evita duplicidad de redaccion entre FR y BC.

### 10.7 Cierre Formal y Politica de Version

- Referencia canonica: BC-023 y BC-024.
