# Research: Gestion de credenciales y roles de empleado

## Decision 1: Persistencia de contrasena con hash irreversible
- Decision: Guardar contrasena como hash irreversible (BCrypt) y validar por comparacion de hash.
- Rationale: Cumple regla constitucional de no exponer credenciales y reduce riesgo de fuga de secretos.
- Alternatives considered:
  - Argon2id: valido, pero mayor costo de ajuste inicial para este alcance.
  - Cifrado reversible: descartado por superficie de riesgo.
  - Texto plano: descartado por incumplimiento de seguridad.

## Decision 2: Esquema obligatorio de autenticacion HTTP Basic
- Decision: Exigir HTTP Basic Authentication (`type=http`, `scheme=basic`) para
  metodos protegidos, usando `correo_electronico` como username.
- Rationale: Alinea implementacion con constitucion vigente y permite una
  verificacion simple, estandar y trazable en contrato API.
- Alternatives considered:
  - Form login/session cookie: descartado por no cumplir el esquema obligatorio.
  - Bearer token en este alcance: descartado por desalineacion con requerimiento
    normativo actual.

## Decision 3: Verificacion de credenciales contra ta 0bla `empleado`
- Decision: Resolver identidad por `correo_electronico` persistido en `empleado`
  y validar `contrasena` transitoria por comparacion de hash derivado contra
  `contrasena_hash` almacenado.
- Rationale: Garantiza no persistir ni exponer contrasena en texto plano y cumple
  con seguridad por hash irreversible.
- Alternatives considered:
  - Persistir contrasena en texto plano: descartado por incumplimiento de seguridad.
  - Cifrado reversible: descartado por mayor superficie de riesgo.

## Decision 4: Matriz de autorizacion por metodo HTTP
- Decision: `ADMIN` para POST/PUT/DELETE; `ADMIN` y `USER` para GET.
- Rationale: Implementa minimo privilegio y coincide con politica constitucional.
- Alternatives considered:
  - Permisos iguales para ambos roles: descartado por no cumplir requerimientos.
  - Jerarquia de roles avanzada: innecesaria para el alcance.

## Decision 5: Migracion de roles con bootstrap controlado
- Decision: Asignar `USER` por defecto a empleados existentes y crear `ADMIN` bootstrap controlado.
- Rationale: Evita privilegios excesivos masivos y asegura continuidad operativa.
- Alternatives considered:
  - Migrar todos a `ADMIN`: riesgo alto de sobreprivilegio.
  - Alta manual de todos los roles antes de despliegue: complejidad operativa elevada.

## Decision 6: Limite de intentos fallidos y bloqueo temporal
- Decision: Umbral de 5 fallos en 15 minutos por correo+IP; bloqueo de 15 minutos.
- Rationale: Balance entre seguridad y usabilidad, con regla verificable en pruebas.
- Alternatives considered:
  - Sin rate limiting: mayor riesgo de fuerza bruta.
  - Bloqueo largo (24h): impacto operativo excesivo.

## Decision 7: Semantica HTTP para autenticacion y bloqueo
- Decision: Responder `401` para credenciales invalidas y `423` para identidad bloqueada temporalmente.
- Rationale: Distingue fallos de autenticacion versus estado de bloqueo y mejora observabilidad.
- Alternatives considered:
  - Solo `401` para ambos casos: ambiguedad operacional.
  - `429` para bloqueo: posible, pero menos directo para estado de recurso bloqueado.

## Decision 8: Unicidad de correo case-insensitive
- Decision: Aplicar normalizacion de correo insensible a mayusculas/minusculas para unicidad y autenticacion.
- Rationale: Evita duplicados funcionales y comportamientos inconsistentes de login.
- Alternatives considered:
  - Unicidad sensible a mayusculas: propensa a cuentas duplicadas semanticas.

## Decision 9: Versionado `v1`/`v2` con retiro programado
- Decision: Mantener `v1` deprecada durante coexistencia temporal; `v2` activa; retiro de `v1` a 90 dias de liberar `v2`.
- Rationale: Minimiza ruptura de clientes y fija un horizonte de migracion obligatorio.
- Alternatives considered:
  - Corte inmediato a `v2`: alto riesgo de impacto a consumidores.
  - Sin fecha de retiro: deuda tecnica indefinida.

## Decision 10: Paginacion por defecto en colecciones
- Decision: Definir `page`, `size`, `sort` con default `size=20` y maximo `size=100`.
- Rationale: Cumple constitucion y protege rendimiento.
- Alternatives considered:
  - Sin paginacion: incumplimiento constitucional.

## Decision 11: Correccion de desalineacion modelo-esquema
- Decision: Crear migraciones nuevas (no reescribir historicas) para `correo_electronico`, `contrasena_hash`, `rol`, `activo` y reglas de unicidad.
- Rationale: Mantiene trazabilidad de Flyway y evita inconsistencias en entornos ya desplegados.
- Alternatives considered:
  - Editar V1: descartado por anti-patron en migraciones versionadas.
