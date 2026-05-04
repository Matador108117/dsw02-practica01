# Implementation Plan: Gestion de credenciales y roles de empleado

**Branch**: `004-empleado-auth-roles` | **Date**: 2026-03-14 | **Spec**: `/specs/004-empleado-auth-roles/spec.md`
**Input**: Feature specification from `/specs/004-empleado-auth-roles/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Implementar autenticacion obligatoria por HTTP Basic para endpoints protegidos,
usando `correo_electronico` como username y `contrasena` transitoria verificada
contra `contrasena_hash` persistido en la tabla `empleado`; aplicar autorizacion
por rol (`ADMIN` CRUD, `USER` solo lectura), control de intentos fallidos con
bloqueo temporal, y versionado `v1`/`v2` con sunset UTC y `410 Gone` post-sunset.
El alcance incluye migraciones de base de datos, contrato OpenAPI,
paginacion por defecto y evidencia de pruebas de integracion/contrato/rendimiento.
La evolucion se mantiene en la linea de version actual (`v2`) y no requiere nueva
version mayor mientras no exista ruptura contractual publica.

## Technical Context

**Language/Version**: Java 17  
**Primary Dependencies**: Spring Boot 3, Spring Security, Spring Data JPA,
springdoc-openapi, Flyway, Jakarta Validation  
**Storage**: PostgreSQL (Docker-managed in local and CI)  
**Testing**: JUnit 5, Spring Boot Test, MockMvc/Testcontainers when applicable  
**Target Platform**: Linux server
**Project Type**: backend web-service  
**Performance Goals**:
- P95 <= 2000 ms para autenticacion valida.
- P95 <= 800 ms para listados paginados con tamano por defecto.
- 100% de escrituras con rol `USER` rechazadas sin efectos laterales.  
**Constraints**: Mandatory HTTP Basic auth (`type=http`, `scheme=basic`) on API
methods using `correo_electronico` as username and transient `contrasena`
validated against persisted `contrasena_hash`, role-based access (`USER`
read-only, `ADMIN` CRUD), OpenAPI/Swagger required, Docker parity for DB,
API major versioning (`/api/v{major}`), paginated list endpoints,
workflow con ramas `feature/<id>-<short-name>` / `fix/<id>-<short-name>` /
`chore/<scope>`, checklist obligatorio en PR, migraciones incrementales sin
reutilizar version ni sobrescribir historico, y cierre formal de feature por
comite tecnico  
**Scale/Scope**:
- Recurso principal: `empleado`.
- Versiones: `v1` (deprecada) y `v2` (activa).
- Ventana de deprecacion: 90 dias naturales desde `release_v2_at_utc`.
- Enforzamiento de sunset: `410 Gone` para `v1` post-sunset con UTC como unico reloj.
- Unicidad de correo: case-insensitive.
- Politica de intentos: 5 fallos/15 min por correo+IP, bloqueo 15 min.
- Integridad de migraciones: usar siguiente numero libre, sin sobrescritura ni
  deriva entre entornos.
- Cierre de feature: commit etiquetado + acta tecnica + aprobacion de comite.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- Stack gate: PASS. Spring Boot 3 + Java 17.
- Security gate: PASS. HTTP Basic obligatorio, `correo_electronico` como username,
  `contrasena` transitoria con verificacion por hash, matriz de roles y semantica
  `401`/`423`.
- Data gate: PASS. Cambios Flyway y esquema PostgreSQL documentados.
- Employee data gate: PASS. `correo_electronico` y `contrasena_hash` requeridos;
  `contrasena` solo de entrada y no persistida en texto plano.
- Contract gate: PASS. OpenAPI/Swagger con seguridad y versionado v1/v2.
- Quality gate: PASS. Plan de pruebas para authN/authZ, DB y contrato.
- Versioning gate: PASS. Versionado mayor `/api/v{major}` con impacto de migracion.
- Sunset gate: PASS. Corte UTC de `v1` con `410 Gone` post-sunset.
- Pagination gate: PASS. Parametros y limites de paginacion definidos.
- Workflow gate: PASS. Convenciones de rama, trazabilidad task->branch->PR y
  checklist obligatorio de PR definidos.
- Migration integrity gate: PASS. Migraciones incrementales sin reutilizar
  version ni sobrescribir historico.
- Feature closure gate: PASS. Cierre formal de feature definido y verificable.

## Project Structure

### Documentation (this feature)

```text
specs/004-empleado-auth-roles/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
│   └── empleados-auth-v2.openapi.yaml
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)
```text
src/main/java/
└── com/dsw02/empleados/
    ├── config/
    ├── controller/
    │   └── dto/
    ├── model/
    ├── repository/
    └── service/

src/main/resources/
├── application.yml
└── db/migration/

src/test/java/
├── unit/
├── integration/
└── contract/

docker/
└── docker-compose.yml
```

**Structure Decision**: Mantener arquitectura monolitica por capas de Spring Boot
ya existente, incorporando cambios de seguridad, persistencia de credenciales,
control de versiones y contrato API sin introducir modulos externos adicionales.

## Phase 0: Research Output

- Ver `/specs/004-empleado-auth-roles/research.md`.

## Phase 1: Design Output

- Ver `/specs/004-empleado-auth-roles/data-model.md`.
- Ver `/specs/004-empleado-auth-roles/contracts/empleados-auth-v2.openapi.yaml`.
- Ver `/specs/004-empleado-auth-roles/quickstart.md`.
- Decision de arquitectura: mantener `ApiVersionSupportPolicy` como entidad
  obligatoria de runtime para enforcement de sunset en UTC.

## Post-Design Constitution Check

- Stack gate: PASS.
- Security gate: PASS.
- Data gate: PASS.
- Employee data gate: PASS.
- Contract gate: PASS.
- Quality gate: PASS.
- Versioning gate: PASS.
- Sunset gate: PASS.
- Pagination gate: PASS.
- Workflow gate: PASS.
- Migration integrity gate: PASS.
- Feature closure gate: PASS.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | N/A | N/A |
