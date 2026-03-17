# Feature Specification: Correccion de acceso global en Swagger UI

**Feature Branch**: `001-swagger-global-auth`  
**Created**: 2026-03-16  
**Status**: Draft  
**Input**: User description: "modifica la especificacion actual para que respete correcciones de acceso global en Swagger UI, autenticacion centralizada y usuario de prueba obligatorio"

## Clarifications

### Session 2026-03-16

- Q: Que esquema de seguridad global debe usar Swagger UI? -> A: HTTP Basic global
- Q: Si ya existe el email del usuario de prueba, como debe actuar el bootstrap? -> A: Normalizar registro existente al estado requerido
- Q: Debe existir endpoint de bootstrap en runtime? -> A: No; bootstrap interno solo al arranque
- Q: Como se determina expiracion de sesion en Swagger UI? -> A: Valida mientras backend acepte; re-Authorize al primer 401
- Q: Debe protegerse tambien /v3/api-docs? -> A: No; publico de solo lectura, ejecucion protegida por auth

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Autorizacion unica en Swagger (Priority: P1)

Como consumidor interno de la API, quiero autenticarme una sola vez con el boton
Authorize para usar todos los endpoints protegidos sin repetir login por metodo.

**Why this priority**: El flujo actual bloquea pruebas funcionales porque obliga a
autenticarse por operacion y rompe la experiencia esperada de Swagger.

**Independent Test**: Puede probarse abriendo Swagger, validando bloqueo inicial,
haciendo una sola autenticacion valida y ejecutando multiples metodos protegidos
sin nuevas ventanas de login.

**Acceptance Scenarios**:

1. **Given** Swagger UI abierto sin sesion autenticada, **When** se visualizan los
  endpoints, **Then** todos aparecen bloqueados y ningun metodo protegido se puede
  ejecutar.
2. **Given** Swagger UI con autenticacion valida realizada desde Authorize,
  **When** se invocan multiples metodos protegidos, **Then** todos se ejecutan sin
  solicitar login adicional por endpoint mientras la sesion sea valida.

---

### User Story 2 - Bloqueo global consistente (Priority: P2)

Como responsable de seguridad, quiero que el esquema de seguridad se aplique de
forma global en la documentacion API para evitar endpoints parcialmente
desprotegidos.

**Why this priority**: Reduce el riesgo de exposicion accidental de metodos por
configuraciones inconsistentes por endpoint.

**Independent Test**: Puede probarse revisando la definicion de seguridad global,
verificando que todos los endpoints protegidos heredan esa politica y que solo las
excepciones permitidas quedan abiertas.

**Acceptance Scenarios**:

1. **Given** la especificacion OpenAPI publicada, **When** se inspecciona su
  configuracion de seguridad, **Then** existe una politica global de autenticacion
  aplicada al documento completo y no una replicacion manual endpoint por endpoint.
2. **Given** una solicitud a endpoint protegido sin credenciales validas,
  **When** el backend recibe la llamada, **Then** la ejecucion del controlador se
  bloquea por validacion previa de seguridad.

---

### User Story 3 - Usuario de prueba reutilizable (Priority: P3)

Como equipo de QA, quiero contar con un usuario de prueba obligatorio y estable
para validar autenticacion desde Swagger en cualquier despliegue.

**Why this priority**: Asegura una prueba repetible de punta a punta sin depender de
creacion manual de datos.

**Independent Test**: Puede probarse iniciando la aplicacion en una base vacia y en
una base con datos existentes para verificar creacion idempotente y login exitoso
del usuario obligatorio.

**Acceptance Scenarios**:

1. **Given** un entorno sin empleado de prueba, **When** inicia la aplicacion,
  **Then** se crea automaticamente el empleado obligatorio con rol USER y
  contrasena almacenada como hash.
2. **Given** un entorno donde el empleado de prueba ya existe, **When** inicia la
  aplicacion, **Then** no se crean duplicados y el flujo de autenticacion sigue
  disponible.

---

### Edge Cases

- Se autentica correctamente en Authorize pero la sesion expira antes de ejecutar
  un metodo: el endpoint vuelve a estado no autorizado hasta nuevo login.
- El usuario intenta ejecutar metodos protegidos sin usar Authorize: la ejecucion
  se rechaza de forma uniforme en todos los endpoints protegidos.
- Se elimina el modal de login por operacion y persiste un unico flujo de
  autenticacion centralizada.
- El bootstrap del usuario de prueba se ejecuta multiples veces: no debe duplicar
  registros ni degradar credenciales existentes validas.
- Si existe registro con mismo email de prueba y datos incompletos, el sistema debe
  normalizarlo al estado requerido sin exponer contrasena en texto plano.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Al abrir Swagger UI o el descriptor API, el sistema MUST mostrar todos
  los endpoints protegidos en estado bloqueado por defecto.
- **FR-001a**: El descriptor `/v3/api-docs` MAY ser accesible en modo lectura para
  documentacion, pero MUST NOT permitir ejecucion de operaciones protegidas sin
  autenticacion valida.
- **FR-002**: Ningun endpoint protegido MUST ejecutarse sin autenticacion valida
  activa.
- **FR-003**: El boton Authorize MUST ser el unico punto de entrada para
  autenticacion interactiva en Swagger UI.
- **FR-004**: Tras una autenticacion exitosa en Authorize, la credencial de sesion
  MUST aplicarse automaticamente a todas las solicitudes protegidas iniciadas desde
  Swagger UI.
- **FR-005**: Mientras la sesion autenticada sea valida, el sistema MUST NOT
  solicitar autenticacion adicional por endpoint o por metodo individual.
- **FR-006**: La gestion de sesion en Swagger UI MUST seguir una regla general de
  validez dependiente de la aceptacion de credenciales por parte del backend; al
  invalidarse la sesion, el sistema MUST volver a exigir autenticacion global antes
  de ejecutar endpoints protegidos.
- **FR-006a**: Como regla operacional verificable, ante el primer `401` por
  credencial invalida o expirada en un endpoint protegido, Swagger MUST requerir
  nuevo Authorize para continuar con operaciones protegidas.
- **FR-007**: La especificacion API MUST definir HTTP Basic como esquema unico de
  seguridad y MUST aplicarlo de forma global al documento.
- **FR-008**: El backend MUST validar la autenticacion antes de permitir la
  ejecucion de controladores para cualquier endpoint protegido.
- **FR-009**: Entre los endpoints de negocio, unicamente el endpoint de login MAY
  estar exento de autenticacion obligatoria.
- **FR-009b**: Endpoints tecnicos de documentacion en modo solo lectura MAY
  declararse publicos cuando se documenten explicitamente (por ejemplo
  `/v3/api-docs`) y MUST NOT habilitar ejecucion de operaciones protegidas sin
  autenticacion valida.
- **FR-009a**: El bootstrap inicial del usuario de prueba MUST ejecutarse como
  proceso interno al arranque y MUST NOT exponerse como endpoint en runtime.
- **FR-010**: El sistema MUST eliminar el comportamiento de autenticacion redundante
  por operacion individual en Swagger UI.
- **FR-011**: El sistema MUST crear automaticamente un empleado de prueba si no
  existe, usando los datos obligatorios: nombre `prueba2`, email
  `emailprueba@gmail.com`, contrasena de acceso `contra123` y rol `USER`.
- **FR-012**: La contrasena del empleado de prueba MUST persistirse como
  `contrasena_hash` y MUST NOT almacenarse en texto plano.
- **FR-013**: La creacion automatica del empleado de prueba MUST ser idempotente.
- **FR-013a**: Si el email de prueba ya existe, el proceso de bootstrap MUST
  normalizar el registro al estado requerido (nombre esperado, rol `USER`,
  credencial valida en `contrasena_hash` y atributos obligatorios completos) sin
  crear duplicados.
- **FR-014**: El empleado de prueba obligatorio MUST poder autenticarse
  exitosamente desde Swagger UI.
- **FR-015**: Esta correccion MUST conservar contrato publico, estructura de
  endpoints vigentes; MAY ajustar persistencia interna y componentes de seguridad
  cuando sea necesario para cumplir hash-only, bootstrap idempotente y seguridad
  global sin alterar el contrato publico.
- **FR-016**: Esta correccion MUST mantenerse en `v2` por tratarse de cambio
  interno sin alteracion de contrato publico y MUST NOT iniciar una nueva version
  mayor.

### Backend Constraints *(mandatory)*

- **BC-001**: La solucion MUST ejecutarse en Spring Boot 3 con Java 17.
- **BC-002**: OpenAPI MUST declarar HTTP Basic como esquema de seguridad
  componente reutilizable y MUST aplicarlo en la seccion `security` global del
  documento.
- **BC-003**: La seguridad backend MUST exigir autenticacion en todos los endpoints,
  excepto login y endpoints tecnicos publicos explicitamente documentados como
  lectura (por ejemplo `/v3/api-docs`).
- **BC-004**: El mecanismo de seguridad backend MUST validar credenciales antes de
  la ejecucion de cualquier controlador protegido.
- **BC-005**: La autorizacion MUST mantener `USER` con permisos de solo consulta y
  `ADMIN` con capacidades CRUD completas en endpoints de empleados.
- **BC-006**: El comportamiento en Swagger UI MUST permanecer consistente con la
  experiencia de autenticacion centralizada de la version anterior del sistema.
- **BC-006a**: El baseline observable de consistencia MUST definirse por estos
  criterios minimos verificables: bloqueo inicial global de endpoints protegidos,
  un unico flujo Authorize para sesion activa y ausencia de login por operacion.
- **BC-007**: La inicializacion del empleado de prueba MUST ejecutarse en arranque y
  MUST ser segura para reinicios repetidos.
- **BC-007a**: La inicializacion idempotente MUST incluir logica de conciliacion
  para actualizar registros de prueba preexistentes incompletos o inconsistentes.
- **BC-007b**: El bootstrap de usuario de prueba MUST ejecutarse fuera de la
  superficie API y MUST NOT introducir rutas nuevas ni bypass de autenticacion.
- **BC-008**: Las pruebas de integracion MUST cubrir bloqueo inicial global,
  autenticacion unica via Authorize, aplicacion automatica de credencial de sesion,
  expiracion de sesion y autenticacion del usuario de prueba.
- **BC-008a**: Las pruebas MUST verificar que una respuesta `401` en endpoint
  protegido invalida el uso continuo de la sesion de Swagger hasta nuevo Authorize.
- **BC-009**: Esta correccion MUST mantener el alcance de versionado en `v2`,
  MUST NOT crear una nueva version mayor y MUST NOT modificar contratos de
  endpoints.

### Key Entities *(include if feature involves data)*

- **Sesion de Swagger**: Estado autenticado temporal usado por Swagger UI para
  adjuntar credenciales validas automaticamente a llamadas protegidas.
- **Politica de Seguridad Global API**: Regla transversal que define el esquema de
  autenticacion de la API y su aplicacion global sobre operaciones protegidas.
- **Empleado de Prueba**: Identidad obligatoria para validacion funcional de
  autenticacion con atributos nombre, email, rol USER y contrasena persistida como
  hash.

## Assumptions

- La arquitectura vigente usa HTTP Basic como mecanismo de autenticacion principal
  y se representa en OpenAPI como esquema global unico.
- El endpoint de login ya existe y permanece como excepcion permitida para
  autenticacion inicial.
- El bootstrap del usuario de prueba es exclusivamente interno al arranque y no
  forma parte del contrato de endpoints.
- El descriptor `/v3/api-docs` puede mantenerse publico para inspeccion del contrato,
  sin implicar autorizacion para ejecutar operaciones protegidas.
- Los atributos no obligatorios del empleado de prueba pueden autocompletarse con
  valores compatibles sin alterar el contrato publico.
- Esta correccion actualiza comportamiento de seguridad y experiencia de Swagger,
  sin ampliar alcance funcional de negocio fuera de autenticacion/autorizacion.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: En validacion funcional, 100% de endpoints protegidos aparecen
  bloqueados al abrir Swagger UI sin autenticacion activa.
- **SC-002**: En validacion funcional, 100% de endpoints protegidos quedan
  utilizables tras una sola autenticacion exitosa en Authorize.
- **SC-003**: En validacion funcional, 0% de operaciones protegidas muestran
  solicitud adicional de login por metodo mientras la sesion sea valida.
- **SC-004**: En validacion funcional, 100% de solicitudes protegidas disparadas
  desde Swagger UI incluyen automaticamente credencial de sesion valida.
- **SC-004a**: En validacion funcional, tras el primer `401` por credencial
  invalida/expirada, 100% de intentos posteriores requieren nuevo Authorize.
- **SC-005**: El usuario de prueba obligatorio logra autenticacion exitosa en 100%
  de ejecuciones de prueba en entornos limpios y reiniciados.
- **SC-006**: Se mantiene 0 cambios en contratos publicos de endpoints, 0 cambios en
  rutas API y 0 cambios de version mayor como resultado de esta correccion.
- **SC-006a**: Se mantiene 100% de trazabilidad de esta correccion dentro de `v2`
  sin apertura de `v3` durante su implementacion, aun cuando existan ajustes
  internos de persistencia/seguridad.
