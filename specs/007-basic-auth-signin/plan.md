# Implementation Plan: HTTP Basic Authentication with Email-Password Login

**Branch**: `007-basic-auth-signin` | **Date**: 2026-03-17 | **Spec**: `/specs/007-basic-auth-signin/spec.md`  
**Input**: Modify Browser Authentication Challenge and Security Scheme in Swagger UI to support email-password login with hash-based password verification.

## Summary

Implement HTTP Basic Authentication as the permanent authentication scheme for the internal API. Clients send email credentials via `Authorization: Basic` header; system authenticates by decoding, searching empleado by email, and comparing password hash using constant-time comparison. All authentication flows are stateless (no session state), with per-email rate limiting (5 failed/min → 5–10 min lockout), strict fail-secure semantics (HTTP 503 on service failure), and comprehensive OpenAPI/Swagger documentation with interactive "Authorize" button. OAuth2/OIDC are explicitly rejected for this monolithic API; federation scenarios (if needed) must use external API Gateway layer, not modify this feature.

## Technical Context

**Language/Version**: Java 17  
**Primary Dependencies**: Spring Boot 3.4.2, Spring Security (HTTP Basic + PasswordEncoder), Spring Data JPA, springdoc-openapi 2.8.4, Bcrypt/Argon2 for password hashing, Flyway for DB migrations  
**Storage**: PostgreSQL 15 (Docker-managed in local dev and CI/CD environments)  
**Testing**: JUnit 5, Spring Boot Test, MockMvc for HTTP testing, Testcontainers for PostgreSQL integration tests  
**Target Platform**: Linux (Docker container orchestration via docker-compose)  
**Project Type**: Backend Spring Boot monolithic web-service API  

**Performance Goals**:
- SC-001: Valid auth requests complete in <100ms
- SC-002: Invalid auth requests complete in <100ms with WWW-Authenticate header
- SC-006: System supports ≥100 concurrent authenticated requests with p99 <200ms
- SC-010: Rate-limiting enforced within 5–10 minute window; HTTP 429 returned before lockout expires
- SC-011: 99.9% monthly uptime; HTTP 503 returned within 1 second on auth service failure

**Constraints**:
- Mandatory HTTP Basic (`type=http, scheme=basic`) on ALL protected endpoints
- Username MUST map to `correo_electronico` persisted in `empleado` table
- Password MUST be transient input, never stored plaintext; compare via hash of `contrasena_hash`
- Role-based authorization: `USER` read-only, `ADMIN` full CRUD
- OpenAPI 3.0 security scheme documentation required
- Stateless authentication (no session tracking, session tokens, cookies, or concurrent session limits)
- Per-email rate limiting: 5 failed attempts per minute → 5–10 minute cooldown
- Fail-secure on auth service failure (HTTP 503, no fallback/bypass)
- HTTPS mandatory in production; HTTP for local dev only with clear warnings
- All credentials must NOT appear in logs, errors, or API responses
- Constant-time hash comparison required (no timing attacks)
- Audit logging of all auth attempts (success/failure) with email + timestamp for 90 days

**Scale/Scope**:
- **Functional**: Implement HTTP Basic authentication flow for internal monolithic API. No external integrations, no multi-tenant federation, no OAuth2.
- **API**: Affects all protected endpoints in `/api/v2/` namespace. No new endpoints; only authentication enforcement on existing routes.
- **Data**: No schema changes to `empleado` table (already has `correo_electronico`, `contrasena_hash`). May add rate-limit tracking table if needed (out-of-scope for MVP).
- **Versioning**: Authentication scheme applies uniformly across all current and future versions; no version-specific behavior.
- **Concurrency**: No concurrent session tracking; Basic Auth is stateless per-request.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- Stack gate: Plan uses Spring Boot 3.4.2 + Java 17 only.
- Security gate: Protected endpoints include mandatory HTTP Basic auth (`type=http`,
  `scheme=basic`) with `correo_electronico` username mapping, constant-time hash
  comparison password verification against persisted `contrasena_hash`, plus role
  policy (`USER` read-only, `ADMIN` CRUD) design and test strategy.
- Data gate: Non-Breaking-change to empleado table (only adds internal rate-limit
  tracking if needed); PostgreSQL + Docker runtime parity ensured.
- Employee data gate: Feature enforces required `correo_electronico` and
  `contrasena_hash` attributes; `contrasena` treated as input-only and never
  persisted in plaintext. No regressions.
- Contract gate: OpenAPI 3.0 HTTP Basic security scheme documented; Swagger UI
  exposes "Authorize" button for interactive testing.
- Quality gate: Integration tests for HTTP Basic auth flow (valid/invalid
  credentials, missing header), role authorization (USER/ADMIN access matrix),
  database access (`empleado` lookup), and OpenAPI contract adherence planned in
  Phase 2 tasks.
- Versioning gate: Uniform auth scheme applied to ALL protected endpoints in
  `/api/v{major}`. No version-specific deviation or breaking changes.
- Sunset gate: Feature does not affect sunset enforcement behavior; `410 Gone`
  policy remains unchanged.
- Pagination gate: Feature does not add or modify list endpoints.
- Workflow gate: Dedicated feature branch `007-basic-auth-signin`, atomic commits
  referencing spec/tasks, PR traceability enforced.

**Gate Result (pre-Phase 0)**: ✅ ALL GATES PASS. No violations or blocking
clarifications. Specification complete; ready for Phase 0 research.

## Project Structure

### Documentation (this feature)

```text
specs/007-basic-auth-signin/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
│   └── empleados-auth-v2.openapi.yaml
├── checklists/
│   └── requirements.md
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
src/main/java/com/dsw02/empleados/
├── config/
│   ├── SecurityConfig.java        # Spring Security HTTP Basic config (NEW)
│   └── OpenApiConfig.java         # Swagger/OpenAPI setup (EXISTING - may update)
├── controller/
│   └── EmpleadoController.java     # Protected endpoints with @PreAuthorize (EXISTING)
├── service/
│   ├── EmpleadoService.java        # Employee business logic (EXISTING)
│   └── EmpleadoUserDetailsService.java  # UserDetailsService impl for auth (NEW)
├── repository/
│   └── EmpleadoRepository.java     # Database access (EXISTING)
└── model/
    ├── Empleado.java              # Employee entity with contrasena_hash (EXISTING)
    └── dto/
        ├── EmpleadoRequest.java    # DTO without passwords (EXISTING)
        └── EmpleadoResponse.java    # DTO without passwords (EXISTING)

src/main/resources/
├── application.yml                # Spring Boot config (may update for auth settings)
└── db/migration/
    └── V[N]__add_rate_limit_table.sql  # Rate-limit tracking table (NEW, optional)

src/test/java/com/dsw02/empleados/
├── unit/
│   └── service/
│       └── EmpleadoUserDetailsServiceTest.java  # Auth logic unit tests (NEW)
├── integration/
│   ├── controller/
│   │   └── EmpleadoControllerAuthIT.java        # HTTP Basic auth integration tests (NEW)
│   └── security/
│       ├── BasicAuthIT.java                     # Full Basic Auth flow tests (NEW)
│       ├── RateLimitingIT.java                  # Rate-limit lockout tests (NEW)
│       └── FailureHandlingIT.java               # Failure/503 scenarios (NEW)
└── contract/
    └── EmpleadoAuthContractIT.java              # OpenAPI contract tests (NEW)

docker/
└── docker-compose.yml             # PostgreSQL + API services (EXISTING - may update)
```

**Structure Decision**: Feature implementation is contained to Spring Security configuration layer (SecurityConfig), authentication service layer (EmpleadoUserDetailsService), and comprehensive integration/contract test coverage. No new database migrations required for basic HTTP Basic auth (rate-limiting table is optional MVP enhancement). Existing EmpleadoController and Entity models are compatible; no refactoring needed.

## Complexity Tracking

> No violations. All gates pass; no justification needed.

---

## Phase Roadmap

### Phase 0: Research & Clarification ✅ COMPLETE

**Deliverable**: `research.md` with architectural decisions documented

**Activities**:
- ✅ Clarification session completed: 5 questions answered (brute-force, uptime SLA, credential mgmt scope, auth rationale, session model)
- ✅ Threat model: HTTP Basic over HTTPS, constant-time hash comparison, rate-limiting per email, fail-secure on failure
- ✅ Architectural decisions documented: permanent Basic Auth (not temporary MVP), no session state, stateless per-request auth
- ✅ Dependencies confirmed: Spring Security HTTP Basic API, bcrypt/Argon2 for hashing, OAuth2 explicitly rejected for this phase
- ✅ Risk mitigation: timing attacks (constant-time comparison), brute-force (rate-limiting), credential exposure (no plaintext logging)

**Outcome**: All ambiguities resolved; ready for Phase 1 design.

### Phase 1: Design & Contracts (IN PROGRESS)

**Deliverables**: `data-model.md`, `contracts/*.openapi.yaml`, `quickstart.md`

**Tasks**:
1. **Entity Design** (`data-model.md`):
   - Empleado model review: `correo_electronico` (unique, case-insensitive), `contrasena_hash` (bcrypt/Argon2)
   - Rate-limit tracking entity (optional): `failed_auth_attempts(email, timestamp, count)`
   - Relationship: 1-to-many (Empleado to failed attempts)

2. **OpenAPI Contracts** (`contracts/*.openapi.yaml`):
   - Global security scheme: `type: http, scheme: basic`
   - Add WWW-Authenticate header to 401 responses
   - Authorized endpoints: `@PreAuthorize("ROLE_USER or ROLE_ADMIN")`
   - Document: "Use Swagger UI 'Authorize' button to supply Basic Auth credentials"

3. **QuickStart** (`quickstart.md`):
   - Setup: Run docker-compose, start API
   - Test authenticated flow: `curl -u email@example.com:password123 http://localhost:8080/api/v2/empleados`
   - Verify Swagger UI: Visit http://localhost:8080/swagger-ui.html, click Authorize, enter credentials
   - Test unauthenticated rejection: `curl http://localhost:8080/api/v2/empleados` → HTTP 401

4. **Agent Context Update**:
   - Update `.github/copilot-instructions.md` or equivalent with HTTP Basic auth requirements
   - Document: Basic Auth permanent design, OAuth2 rejection, rate-limiting strategy

**Outcome**: Design complete, contracts reviewed, developers can begin Phase 2.

### Phase 2: Implementation & Testing (TO START)

**Tasks Generated by `/speckit.tasks`**: ~45–60 tasks across:
- T1–T5: Spring Security HTTP Basic configuration
- T6–T10: EmpleadoUserDetailsService implementation
- T11–T15: Rate-limiting filter/service
- T16–T20: Global exception handler for 401/403/429 responses
- T21–T30: Integration tests (auth flows, rate-limiting, failures)
- T31–T40: Contract tests (OpenAPI adherence)
- T41–T50: Quick-start validation & documentation
- T51–60: PR review & merge to main

**Quality Gates**:
- ✅ All integration tests pass (auth, authz, rate-limiting, failure scenarios)
- ✅ Contract tests pass (OpenAPI scheme, Swagger UI integration)
- ✅ No plaintext passwords in logs or responses
- ✅ Constant-time hash comparison verified via code review
- ✅ Audit logs capture all auth attempts with email + timestamp
- ✅ Per-email rate-limiting enforced & tested
- ✅ HTTPS warnings documented for local HTTP dev use

---

## Known Risks & Mitigation

| Risk | Mitigation |
|------|-----------|
| Passwords leaked in logs | Code review explicitly checks for credential redaction; audit logging uses email + "[redacted]" pattern |
| Timing attack on hash comparison | Spring Security's `PasswordEncoder.matches()` uses constant-time comparison by default |
| Rate-limiting bypass via IP rotation | Rate-limiting keyed per email address, not IP, so IP rotation has no effect |
| Service failure allows fallback auth | Fail-secure design: HTTP 503 immediately on auth service down; no fallback caching |
| Concurrent requests conflict | Stateless auth: each request independently authenticated; no shared session state |
| OAuth2 scope creep later | Permanent design choice documented; federation requires external API Gateway layer, not feature modification |
