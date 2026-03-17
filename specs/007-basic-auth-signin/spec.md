# Feature Specification: HTTP Basic Authentication with Email-Password Login

**Feature Branch**: `007-basic-auth-signin`  
**Created**: 2026-03-17  
**Status**: Draft  
**Input**: Modify Browser Authentication Challenge and Security Scheme in Swagger UI to support email-password login with hash-based password verification.

## User Scenarios & Testing *(mandatory)*

<!--
  IMPORTANT: User stories should be PRIORITIZED as user journeys ordered by importance.
  Each user story/journey must be INDEPENDENTLY TESTABLE - meaning if you implement just ONE of them,
  you should still have a viable MVP (Minimum Viable Product) that delivers value.
  
  Assign priorities (P1, P2, P3, etc.) to each story, where P1 is the most critical.
  Think of each story as a standalone slice of functionality that can be:
  - Developed independently
  - Tested independently
  - Deployed independently
  - Demonstrated to users independently
-->

### User Story 1 - Authenticate with Valid Email and Password (Priority: P1)

API clients (including Swagger UI) send HTTP requests with Basic Auth credentials (correo_electronico:password). The system decodes the credentials, searches for an empleado record by `correo_electronico`, validates the provided password against the stored `contrasena_hash` using secure hash comparison, and authenticates the client if credentials match.

**Why this priority**: This is the core authentication flow; without it, no user can access protected endpoints.

**Independent Test**: Can be tested by sending a valid Basic Auth header with correo_electronico:password and verifying HTTP 200 response from a protected endpoint, independent of other features.

**Acceptance Scenarios**:

1. **Given** a valid empleado exists with `correo_electronico=user@example.com` and hashed password, **When** a client sends `Authorization: Basic base64(user@example.com:password123)` to a protected endpoint, **Then** the system verifies the hash match and returns HTTP 200 with endpoint data.
2. **Given** SSL/HTTPS is enabled, **When** a client authenticates over HTTPS with Basic Auth, **Then** credentials are transmitted securely and authentication succeeds.
3. **Given** a client has authenticated, **When** the client makes subsequent requests with the same Basic Auth header, **Then** each request is independently verified without session state.

---

### User Story 2 - Reject Invalid Credentials with Proper Error Response (Priority: P1)

When credentials are missing, malformed, or invalid (email not found or password mismatch), the system returns HTTP 401 Unauthorized with appropriate error context, without exposing details that could aid attacks.

**Why this priority**: Security-critical; proper rejection prevents unauthorized access and provides secure feedback.

**Independent Test**: Can be tested independently by sending invalid credentials (wrong email, wrong password, missing header) and verifying HTTP 401 response, without requiring full authentication flow.

**Acceptance Scenarios**:

1. **Given** a request lacks the `Authorization` header, **When** the request targets a protected endpoint, **Then** the system returns HTTP 401 Unauthorized.
2. **Given** a valid Basic Auth header with non-existent email (e.g., `nonexist@example.com`), **When** the request targets a protected endpoint, **Then** the system returns HTTP 401 without indicating whether email exists.
3. **Given** a valid Basic Auth header with correct email but wrong password, **When** the request targets a protected endpoint, **Then** the system returns HTTP 401 without indicating password mismatch details.
4. **Given** a malformed Basic Auth header (invalid base64 or missing colon separator), **When** the request targets a protected endpoint, **Then** the system returns HTTP 401.

---

### User Story 3 - Document Authentication in Swagger UI (Priority: P2)

The Swagger/OpenAPI specification documents HTTP Basic authentication as the mandatory security scheme. API clients (including the Swagger UI "Authorize" button) can visually inspect and test the authentication flow using the documented scheme.

**Why this priority**: Enables test-driven development and reduces client onboarding friction; improves API discoverability.

**Independent Test**: Can be tested independently by reviewing the OpenAPI specification for the security scheme entry and verifying Swagger UI renders an "Authorize" button, without requiring successful authentication.

**Acceptance Scenarios**:

1. **Given** a user opens the Swagger UI at `/swagger-ui.html`, **When** the page loads, **Then** an "Authorize" button appears in the top-right corner.
2. **Given** a user clicks the "Authorize" button, **When** the dialog appears, **Then** it prompts for username (email) and password with HTTP Basic scheme context.
3. **Given** a user enters valid email and password in the Authorize dialog, **When** they click "Authorize", **Then** subsequent requests in Swagger UI include the `Authorization: Basic` header with the credentials.
4. **Given** a user clicks "Logout" in Swagger UI, **When** the credentials are cleared, **Then** subsequent requests no longer include the `Authorization` header.

### Edge Cases

- What happens when email contains special characters (e.g., `user+tag@example.com`)? → System MUST treat as valid email per RFC 5321.
- What happens when password contains Base64-reserved characters (e.g., `+`, `/`, `=`)? → System MUST handle correctly after decoding.
- What happens when multiple requests with different credentials are sent in rapid succession? → Each request MUST be authenticated independently without cache/session conflicts.
- What happens on extremely high authentication traffic? → System MUST maintain consistent hash-comparison time (constant-time comparison to prevent timing attacks).
- What happens after 5 failed login attempts in one minute for a given email? → System MUST temporarily lock that email for 5–10 minutes with exponential backoff.

## Requirements *(mandatory)*

<!--
  ACTION REQUIRED: The content in this section represents placeholders.
  Fill them out with the right functional requirements.
-->

### Functional Requirements

- **FR-001**: System MUST support HTTP Basic Authentication (`type=http`, `scheme=basic`) for all protected endpoints.
- **FR-002**: System MUST decode `Authorization: Basic base64(email:password)` header and extract email and password.
- **FR-003**: System MUST search the `empleado` table by `correo_electronico` (case-insensitive email lookup per email RFC).
- **FR-004**: System MUST reject authentication if empleado not found, returning HTTP 401 without revealing whether email exists.
- **FR-005**: System MUST validate the decoded password by comparing its secure hash against stored `contrasena_hash` using constant-time comparison to prevent timing attacks.
- **FR-006**: System MUST reject authentication if hash comparison fails, returning HTTP 401.
- **FR-007**: System MUST return HTTP 401 Unauthorized with `WWW-Authenticate: Basic realm="empleados-api"` header for rejected requests.
- **FR-008**: System MUST accept and authenticate valid credentials without state persistence (stateless authentication per Basic Auth semantics).
- **FR-009**: System MUST include HTTP Basic security scheme in OpenAPI 3.0 specification (`type: http`, `scheme: basic`).
- **FR-010**: System MUST document security requirement (HTTPS mandatory) in feature spec and migration notes.
- **FR-011**: System MUST apply HTTP Basic authentication to ALL protected endpoints unless feature spec explicitly marks endpoint as public with constitutional justification.
- **FR-012**: System MUST log authentication attempts (success/failure) with email and timestamp for security auditing.
- **FR-013**: System MUST implement brute-force protection by rate-limiting failed authentication attempts: maximum 5 failed attempts per correo_electronico per minute, followed by exponential backoff (5–10 minute temporary lock) on that correo_electronico.
- **FR-014**: System MUST explicitly exclude password reset, account unlock, and credential administration endpoints from scope. These capabilities are deferred to a future "Admin Dashboard" or "Credential Management" feature.
- **FR-015**: System MUST operate as **stateless** with respect to authentication: each HTTP request containing a valid `Authorization: Basic` header is independently authenticated without server-side session state. Clients MAY cache credentials locally (e.g., in Swagger UI browser storage); logout is a client-side operation (credential removal from client storage). Server MUST NOT implement session tracking, token issuance, or concurrent session limits.

### Backend Constraints

- **BC-001**: Solution MUST run on Spring Boot 3 with Java 17 per Constitution Principle I.
- **BC-002**: HTTP Basic authentication MUST be implemented via Spring Security's `HttpBasic` configuration per Constitution Principle II.
- **BC-002a**: Basic Auth username MUST map to persisted `correo_electronico` in `empleado` table per Constitution Principle II.
- **BC-002b**: Basic Auth password MUST be transient input and MUST be validated by comparing derived secure hash against persisted `contrasena_hash` per Constitution Principle II.
- **BC-003**: Authorization MUST enforce `USER` read-only access and `ADMIN` full CRUD access per Constitution Principle II.
- **BC-004**: Data persistence MUST target PostgreSQL per Constitution Principle III.
- **BC-005**: Local and CI database execution MUST be Docker-based per Constitution Principle III.
- **BC-006**: The `empleado` table schema MUST enforce non-null `correo_electronico` and non-null `contrasena_hash` columns per Constitution Principle III.
- **BC-007**: `contrasena` plaintext MUST NOT be stored in any persistent database column per Constitution Principle III.
- **BC-008**: API changes MUST include OpenAPI/Swagger documentation updates per Constitution Principle IV.
- **BC-009**: Spec MUST state required integration tests for HTTP Basic auth, role authorization, database access, and OpenAPI contract per Constitution Principle V.
- **BC-010**: Public API endpoints MUST be versioned with `/api/v{major}` per Constitution Principle VI.
- **BC-011**: Collection endpoints MUST define pagination parameters plus default and maximum page limits per Constitution Principle VII.
- **BC-012**: Implementation workflow MUST adhere to feature branch discipline and PR traceability per Constitution Principle VIII.
- **BC-013**: Password hashing MUST use industry-standard algorithm (e.g., bcrypt, Argon2) with configurable work factor; plain hashing (MD5, SHA-1) MUST NOT be used.
- **BC-014**: Password comparison MUST use constant-time comparison to prevent timing attacks (e.g., `javax.crypto.Cipher` or Spring Security's `PasswordEncoder.matches()`).
- **BC-015**: Credentials MUST NOT appear in logs, error messages, or API responses; only email (for auditing) and generic "invalid credentials" may be logged.
- **BC-016**: Basic Auth transmission MUST be over HTTPS in production; HTTP MUST only be used for local development/testing with clear warnings.
- **BC-017**: Rate limiting MUST be implemented per correo_electronico address (not per IP): track failed attempts, allow maximum 5 per minute, and apply exponential backoff cooldown (5–10 minutes) on lockout; successful authentication MUST reset the failed attempt counter for that correo_electronico.
- **BC-018**: System failures (database unavailable, auth service down) MUST NOT trigger fallback/bypass mechanisms; instead, authentication requests MUST fail fast with HTTP 503 (Service Unavailable) within 1 second without attempting any access or caching credentials.
- **BC-019**: HTTP Basic Authentication is the permanent and exclusive authentication scheme for this API. OAuth2, OIDC, JWT, and token-based authentication are explicitly NOT supported and are NOT planned for future introduction to this endpoint. Federation and multi-app scenarios requiring OAuth2 MUST use a separate API gateway or middleware layer; they MUST NOT drive changes to this feature's authentication model.
- **BC-020**: Authentication is **stateless**: the system MUST NOT implement session management, session tokens, cookies, or concurrent session limits. Each request is independently authenticated by decoding and validating the `Authorization: Basic` header. Logout is a client-side operation (credential removal from client storage); the server has no logout endpoint or session invalidation logic.

### Non-Functional Requirements

- **NFR-001**: System uptime MUST target 99.9% monthly availability (max ~43 minutes downtime/month).
- **NFR-002**: Recovery Time Objective (RTO) on complete outage MUST be under 15 minutes after failure recovery.
- **NFR-003**: Mean Time To Detection (MTTD) of authentication service degradation MUST be under 30 seconds via automated monitoring.

## Design Rationale & Trade-offs

### Why HTTP Basic Authentication?

HTTP Basic Authentication is chosen as the **permanent and exclusive** authentication scheme for this API because:

1. **Simplicity**: Zero session state, zero token management, zero refresh token lifecycle. Clients encode `email:password` in header; server verifies instantly. Minimal operational complexity.
2. **Monolithic API Scope**: This API is internal to a single monolithic Spring Boot application serving one frontend (Swagger UI + internal dashboards). Multi-app federation, external integrations, and OAuth2 flows are **not required** for current and foreseeable business use cases.
3. **Security Sufficiency**: Combined with HTTPS, constant-time hash comparison, and per-email rate limiting, Basic Auth provides adequate security for employee/internal user authentication without the overhead of token issuance, rotation, and revocation.
4. **Regulatory Alignment**: HTTP Basic over HTTPS meets standard internal-API security expectations without triggering OAuth2 risk/complexity for internal use.

### Explicitly Rejected Alternatives

**OAuth2/OIDC**: Rejected because they introduce session/token lifecycle complexity (issuance, refresh, revocation, expiry), require additional infrastructure (token storage, refresh token rotation), and solve multi-app federation problems that are **out of scope** for this monolithic API. If federated multi-app authentication becomes necessary in the future, a **separate API Gateway or middleware layer** MUST be introduced to handle OAuth2 translation; this feature's Basic Auth implementation MUST NOT be modified.

**JWT**: Rejected for similar reasons (token lifecycle, signature verification overhead, revocation complexity) without federation benefits of OAuth2. Not suitable for internal monolithic API with direct password access.

### Migration Path for Future Scenarios

If external API consumers or multi-app federation requirements emerge in the future:
1. **Do NOT modify this feature** to add OAuth2/token support.
2. **Instead, introduce an API Gateway** (e.g., Kong, Nginx reverse proxy, AWS API Gateway) that handles OAuth2 client authentication and translates it to internal HTTP Basic calls, maintaining this feature's single-purpose design.
3. Document the gateway's OAuth2 → Basic Auth translation as a separate architectural layer.

**Rationale**: Preserving this feature's single-purpose, simple design ensures long-term maintainability and prevents scope creep from federation requirements.

## Key Entities

- **Empleado**: Represents an employee/user with authentication credentials.
  - `correo_electronico` (String, required, unique, case-insensitive): Email address used as Basic Auth username.
  - `contrasena_hash` (String, required): Secure hash of the employee's password, never stored plaintext.
  - `id` (Long, required): Unique identifier.
  - `nombre` (String, required): Employee name.
  - `rol` (Enum: USER | ADMIN, required): Authorization role for CRUD access matrix.

## Success Criteria *(mandatory)*

<!--
  ACTION REQUIRED: Define measurable success criteria.
  These must be technology-agnostic and measurable.
-->

### Measurable Outcomes

- **SC-001**: Clients sending valid email:password via Basic Auth header receive HTTP 200 from protected endpoints within 100ms.
- **SC-002**: Clients sending invalid credentials receive HTTP 401 within 100ms; response includes `WWW-Authenticate` header.
- **SC-003**: All protected endpoints (100% of non-public routes) enforce HTTP Basic authentication.
- **SC-004**: Swagger UI renders "Authorize" button and allows users to test authentication interactively without external tools.
- **SC-005**: OpenAPI specification documents `type: http`, `scheme: basic` security scheme for all protected endpoints.
- **SC-006**: Authentication system supports at least 100 concurrent requests with consistent latency (p99 under 200ms).
- **SC-007**: No plaintext passwords appear in database, logs, or API responses during normal operation.
- **SC-008**: Constant-time hash comparison is implemented; timing differences between valid and invalid passwords are less than 10ms (platform-dependent baseline).
- **SC-009**: Audit logs record all authentication attempts (success/failure) with email and timestamp for 90 days of retention.
- **SC-010**: Rate limiting is enforced: after 5 failed attempts per email per minute, further attempts are blocked with HTTP 429 (Too Many Requests) for 5–10 minutes; successful login resets the counter.
- **SC-011**: System availability meets 99.9% monthly uptime target; authentication failures due to service unavailability return HTTP 503 within 1 second without credential caching fallback.
- **SC-012**: System operates statelessly: there are NO session endpoints, NO logout endpoints, NO concurrent session limits, and NO cookie/token issuance. Multiple requests with the same Basic Auth credentials are independently authenticated. Swagger UI "Logout" removes credentials from client storage only.

## Clarifications

### Session 2026-03-17

- Q1: Should the system defend against brute-force login attempts via rate limiting or backoff? → A: Yes, implement per-email rate limiting with maximum 5 failed attempts per minute, followed by 5–10 minute exponential backoff cooldown.
- Q2: What uptime SLA and failure handling strategy? → A: Strict SLA approach: target 99.9% monthly uptime with fail-secure semantics (no caching or fallback); database down → HTTP 503 immediately.
- Q3: Should credential management (password reset, unlock) be in-scope? → A: No, out-of-scope; deferred to future "Admin Dashboard" feature. This feature covers authentication enforcement only.
- Q4: What is the architectural rationale for Basic Auth vs OAuth2/OIDC? → A: Basic Auth is the permanent and exclusive choice for this monolithic internal API. OAuth2/OIDC are explicitly rejected; federation scenarios (if they arise) must use an external API Gateway/middleware layer, not modify this feature.
- Q5: How should session/logout/concurrency be handled in Basic Auth? → A: Stateless semantics: each request independently authenticated, no server-side session tracking, no concurrent session limits. Logout is client-side only (credential removal from client storage/Swagger UI).

## Assumptions

- Email addresses are unique and case-insensitive (per RFC 5321).
- Passwords are hashed using bcrypt or Argon2 with at least 10 rounds (bcrypt cost factor).
- The application will run over HTTPS in production; HTTP is acceptable only for local development.
- Empleado records are created/updated by admin workflows outside this feature scope.
- **HTTP Basic Authentication is the permanent and exclusive authentication scheme**; OAuth2, OIDC, and token-based auth are explicitly rejected and not planned for this API.
- If future federation or multi-app scenarios require OAuth2, they MUST be handled by a separate API Gateway/middleware layer; this feature's Basic Auth implementation MUST NOT be modified.
- Role-based authorization (USER/ADMIN) is already implemented or will be implemented in dependent features.
- **Password reset, account unlock, and credential revocation are explicitly out-of-scope** and will be handled by future admin/credential management features.
- Rate-limit lockouts expire automatically after the cooldown period; no administrative unlock necessary.
- **Stateless authentication model**: The system MUST NOT maintain session state, issue tokens/cookies, or enforce concurrent session limits. Clients (Swagger UI, API clients) MAY cache credentials locally in memory/storage for convenience. Logout is purely client-side (remove credentials from client storage). The server has no logout endpoint or session invalidation mechanism.
- Each HTTP request containing valid Basic Auth credentials is independently authenticated without reference to prior requests or memory of previous authentications.

## Open Questions / Clarifications

- [NONE AT THIS TIME - specification is clear and actionable]

## References

- [Constitution Principle II](../../.specify/memory/constitution.md#ii-emailpassword-authentication-and-role-authorization): Email/Password Authentication and Role Authorization
- [Constitution Principle III](../../.specify/memory/constitution.md#iii-postgresql--docker-by-default): PostgreSQL + Docker by Default
- [Constitution Principle IV](../../.specify/memory/constitution.md#iv-contract-driven-api-with-swagger): Contract-Driven API with Swagger  
- [RFC 5321 - SMTP](https://tools.ietf.org/html/rfc5321): Email address syntax
- [RFC 7617 - HTTP Authentication](https://tools.ietf.org/html/rfc7617): The 'Basic' HTTP Authentication Scheme
- [OWASP - Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html): Secure authentication practices
- [Spring Security - HTTP Basic](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/basic.html): Spring Boot HTTP Basic configuration
- [bcrypt](https://en.wikipedia.org/wiki/Bcrypt): Adaptive hashing algorithm for password storage
