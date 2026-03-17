# Research: Correccion de acceso global en Swagger UI

## Decision 1: Esquema de seguridad global en OpenAPI
- Decision: Mantener HTTP Basic (`type=http`, `scheme=basic`) como esquema unico y aplicarlo en `security` a nivel global del documento OpenAPI.
- Rationale: Cumple la constitucion vigente, evita configuracion fragmentada por endpoint y habilita el flujo centralizado de Authorize en Swagger UI.
- Alternatives considered:
  - Bearer JWT: descartado para este alcance porque implica cambio de arquitectura no requerido.
  - Definir seguridad por endpoint: descartado por riesgo de inconsistencia y autenticacion redundante.

## Decision 2: Comportamiento de Swagger UI en sesion autenticada
- Decision: Tratar la sesion de Swagger como valida mientras el backend acepte credenciales; exigir nuevo Authorize al primer `401`.
- Rationale: Alinea el estado de UI con la verdad del backend y elimina prompts de login por metodo.
- Alternatives considered:
  - TTL fijo en UI: descartado por desalineacion potencial con estado real del servidor.
  - Sesion infinita del navegador: descartado por riesgo de seguridad.

## Decision 3: Exposicion de documentacion y proteccion de operaciones
- Decision: Mantener `/v3/api-docs` accesible en lectura para documentacion, preservando autenticacion obligatoria para ejecucion de operaciones protegidas.
- Rationale: Conserva compatibilidad de tooling OpenAPI sin reducir el control de acceso real de la API.
- Alternatives considered:
  - Bloquear `/v3/api-docs`: descartado por friccion operativa innecesaria.
  - Exponer operaciones sin auth en Swagger: descartado por incumplimiento de seguridad.

## Decision 4: Bootstrap del usuario de prueba
- Decision: Crear/normalizar en arranque un usuario obligatorio (`prueba2`, `emailprueba@gmail.com`, rol `USER`) con contrasena persistida solo en `contrasena_hash`, sin endpoint de bootstrap.
- Rationale: Garantiza repetibilidad de pruebas, idempotencia y menor superficie de ataque.
- Alternatives considered:
  - Endpoint de bootstrap en runtime: descartado por riesgo de seguridad.
  - Falla en arranque si usuario existe inconsistente: descartado por fragilidad operativa.

## Decision 5: Alcance de versionado
- Decision: Implementar la correccion dentro de `v2` sin crear `v3`.
- Rationale: El cambio es interno, no altera contrato publico ni estructura de endpoints.
- Alternatives considered:
  - Abrir `v3`: descartado por sobreversionado sin ruptura contractual.

## Decision 6: Cobertura de pruebas de integracion/contrato
- Decision: Incluir pruebas para bloqueo inicial global, Authorize unico, reutilizacion de credenciales en operaciones, invalidacion tras `401`, y autenticacion del usuario de prueba.
- Rationale: Cubre los criterios de aceptacion y los gates de calidad constitucionales.
- Alternatives considered:
  - Solo pruebas manuales en Swagger: descartado por baja repetibilidad en CI.

## Resolved Clarifications
- Todas las decisiones de seguridad, bootstrap y versionado quedaron resueltas en la especificacion; no quedan marcadores `NEEDS CLARIFICATION`.
