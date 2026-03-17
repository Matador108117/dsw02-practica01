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

API clients (including Swagger UI) send HTTP requests with Basic Auth credentials (email:password). The system decodes the credentials, searches for an empleado record by `correo_electronico`, validates the provided password against the stored `contrasena_hash` using secure hash comparison, and authenticates the client if credentials match.

**Why this priority**: This is the core authentication flow; without it, no user can access protected endpoints.

**Independent Test**: Can be tested by sending a valid Basic Auth header with email:password and verifying HTTP 200 response from a protected endpoint, independent of other features.

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

### Key Entities

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

## Assumptions

- Email addresses are unique and case-insensitive (per RFC 5321).
- Passwords are hashed using bcrypt or Argon2 with at least 10 rounds (bcrypt cost factor).
- The application will run over HTTPS in production; HTTP is acceptable only for local development.
- Empleado records are created/updated by admin workflows outside this feature scope.
- Basic Auth is sufficient for this phase; OAuth2/JWT may be added in future features.
- Role-based authorization (USER/ADMIN) is already implemented or will be implemented in dependent features.

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
