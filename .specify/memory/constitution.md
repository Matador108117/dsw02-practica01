<!--
Sync Impact Report
- Version change: 4.1.0 -> 4.2.0
- Modified principles:
	- II. Email/Password Authentication and Role Authorization -> II. Email/Password Authentication and Role Authorization (expanded with Basic+JWT coexistence policy)
	- III. PostgreSQL + Docker by Default -> III. PostgreSQL + Docker by Default (expanded to require frontend Docker and compose integration)
	- V. Testability and Operability Gates -> V. Testability and Operability Gates (expanded with mandatory frontend E2E gates)
	- VI. API Versioning Compatibility -> VI. API Versioning Compatibility (expanded with independent frontend version policy)
- Added sections:
	- XI. Official Frontend Platform
- Removed sections:
	- None
- Templates requiring updates:
	- ✅ updated: .specify/templates/plan-template.md
	- ✅ updated: .specify/templates/spec-template.md
	- ✅ updated: .specify/templates/tasks-template.md
	- ✅ verified: .specify/templates/commands/*.md (directory not present)
	- ✅ updated: .github/agents/copilot-instructions.md
- Follow-up TODOs:
	- None
-->

# DSW02 Practica 01 Constitution

## Core Principles

### I. Backend Spring Standard
All backend services MUST be implemented with Spring Boot 3 and Java 17. New services,
modules, and examples MUST use this baseline stack and compatible dependencies only.
Rationale: a single modern stack reduces integration risk and maintenance cost.

### II. Email/Password Authentication and Role Authorization
All API methods (GET, POST, PUT, DELETE) MUST require HTTP Basic Authentication
(`type=http`, `scheme=basic`) unless a feature specification explicitly marks a
method as public and documents constitutional justification.
For authenticated requests, the Basic Auth username MUST be
`correo_electronico` and the password MUST be the transient `contrasena` input.
Password verification MUST compare a derived secure hash of the transient input
against `contrasena_hash` persisted in the `empleado` table.
Authorization MUST enforce role policies where `USER` has read-only API access and
`ADMIN` has full CRUD access. The employee identity source MUST persist email and a
secure password hash as required credential attributes for each employee record.
`contrasena` MUST be treated as transient input only for registration/authentication
flows and MUST NOT be stored in plaintext. Password storage and verification MUST
follow secure hashing practices and MUST NOT expose raw credentials in logs or API
payloads.
Access matrix for employee API endpoints MUST be explicit and testable:
- `ADMIN`: create, read, update, delete.
- `USER`: read only.

Coexistence policy for authentication mechanisms is mandatory when frontend auth
bootstrap is required:
- HTTP Basic remains mandatory for protected domain endpoints unless explicitly
	overridden by approved feature governance.
- JWT-based authentication MAY be introduced only for explicitly documented auth
	endpoints and MUST NOT weaken role authorization policies.
- JWT flows MUST define token expiry, refresh behavior, revocation path, secure
	storage constraints, and CSRF controls when cookies are used.
Rationale: explicit identity and role boundaries are mandatory for secure API behavior.

### III. PostgreSQL + Docker by Default
Application persistence MUST target PostgreSQL. Local and CI execution MUST rely on
Docker-based database runtime (for example Docker Compose) to guarantee reproducible
environments. The `empleado` table schema MUST enforce non-null
`correo_electronico` and non-null `contrasena_hash` columns for all records.
`contrasena` plaintext MUST NOT be stored in any persistent database column.
Schema relations MUST be explicit using declared foreign keys when an entity
references another persisted entity.
Any official frontend delivery in this project MUST be containerized with Docker
and integrated into the existing Docker Compose stack.
Rationale: consistent runtime parity avoids environment-specific defects and
guarantees security-critical identity data integrity.

### IV. Contract-Driven API with Swagger
HTTP APIs MUST be documented and exposed through OpenAPI/Swagger. Every delivered
endpoint MUST include request, response, and security documentation before release.
Rationale: accurate API contracts reduce onboarding and integration friction.

### V. Testability and Operability Gates
Changes MUST include automated tests proportional to risk, with mandatory integration
coverage for authentication, role-based authorization, database access, and API
contracts. Services MUST emit structured logs for critical flows and startup/runtime
failures.
Official frontend changes MUST include mandatory Cypress E2E execution with at least
these scenarios: successful login, failed login, employee rendering, department
rendering, and CRUD operations. No build is valid unless required E2E tests pass.
Rationale: reliability depends on fast feedback and diagnosable production behavior.

### VI. API Versioning Compatibility
Public HTTP endpoints MUST be versioned in the URI path using the `/api/v{major}`
pattern. Backward-incompatible API changes MUST increment the major version and MUST
be documented in OpenAPI plus migration notes in feature specs. Backward-compatible
changes MUST preserve existing version behavior. When a deprecated version reaches its
declared sunset timestamp, requests to that version MUST return `410 Gone` and MUST be
enforced automatically with UTC as the only business clock. Rationale: explicit API
versioning protects clients from accidental breaking changes.

Any change that modifies the public contract (request/response shape, entity graph,
or route set), or adds new public endpoints, SHALL be treated as backward-incompatible
for governance and MUST trigger a major API version increment.

Current official public major for the domain model that includes `Departamento` is
`v3`.

Official public major for auth bootstrap endpoints using JWT coexistence is `v4`.

When an official frontend is present, frontend release versioning MUST remain
independent from backend API versioning.

### VII. API Pagination by Default
All collection/list endpoints MUST implement pagination with explicit request parameters
and metadata in responses. Defaults and maximum page size limits MUST be documented in
OpenAPI and validated in contract/integration tests. Endpoints that intentionally avoid
pagination MUST include written justification in the feature spec. Rationale: pagination
ensures predictable performance and stable API consumption.

### VIII. Git Workflow Discipline
Every change MUST be developed in a dedicated feature branch tied to a spec identifier.
Commits MUST be atomic, descriptive, and traceable to tasks or requirements. Pull
requests MUST include evidence for tests, API contract updates, and constitutional
compliance. Direct commits to the main branch are forbidden except for approved release
automation. Rationale: disciplined Git usage improves traceability, review quality,
and release safety.

### IX. Automated Agent Workflow Discipline
Automated agents (implementation/build/test agents) MUST work on the current branch as
checked out unless the user explicitly instructs the agent to create or switch branches.
Agents MUST NOT create feature branches or checkout different branches without explicit
user instruction. When executing multi-phase implementations (such as feature specs with
Phase 1, 2, 3 structure), agents MUST complete each assigned phase to completion despite
transient repository changes (such as file modifications, new commits by other processes,
or CI/CD pipeline updates). Agents MUST NOT abort or halt execution without explicit
user instruction to do so. Agents MUST document progress, blockers, and recovery actions
in visible task tracking and summaries before continuing. Rationale: predictable agent
behavior ensures that implementation phases complete reliably even in multi-process
environments, and explicit instruction prevents accidental task abandonment.

### X. Domain Relational Integrity (Departamento-Empleado)
The official domain model includes `Departamento` as a persisted entity.

Relational rules are mandatory:
- A `Departamento` MAY have multiple `Empleado` records.
- An `Empleado` MUST reference at most one `Departamento`.
- An `Empleado` MAY be created or updated without department assignment
	(`departamento_id` nullable), but when assignment exists it MUST reference a
	valid persisted `Departamento` row.
- Implicit relations without declared foreign key constraints are forbidden.

Contract evolution rules are mandatory:
- Features introducing `Departamento` resources or department-related employee
	projections MUST publish contract updates in OpenAPI and migration notes.
- These changes MUST ship under API major `v3` or later, never under older majors.

Rationale: explicit relational integrity prevents orphaned references and keeps API
and persistence behavior deterministic under schema evolution.

### XI. Official Frontend Platform
The project formally includes an official frontend as part of the delivery
ecosystem. This frontend MUST use Angular 22 LTS, TypeScript, and nvm for Node
version management.

Integration rules are mandatory:
- The frontend SHALL consume only official API endpoints.
- Frontend business logic duplication of backend domain rules is forbidden.
- Authentication SHALL be delegated to the API.
- The frontend MUST integrate with the existing Docker Compose topology.

Testing and release rules are mandatory:
- Cypress is the required E2E framework.
- E2E coverage MUST include successful login, failed login, employee rendering,
	department rendering, and CRUD operations.
- Builds MUST fail when required E2E tests fail or are skipped.
- Frontend versioning SHALL be independent from backend and SHOULD follow the
	pattern `vMAJOR.MINOR.PATCH-front` (for example `v1.0.0-front`).

Rationale: a single governed frontend platform ensures consistent client behavior,
prevents logic drift, and enforces deployment parity across environments.

## Technical Constraints

- Runtime MUST be Java 17.
- Framework MUST be Spring Boot 3.
- Persistence MUST be PostgreSQL.
- Local/CI database runtime MUST be Docker-managed.
- Official frontend runtime MUST be Docker-managed and wired into existing compose.
- API documentation MUST be available via Swagger UI.
- Security for protected routes MUST use HTTP Basic (`type=http`, `scheme=basic`).
- JWT MAY be used for explicit auth endpoints when approved by feature governance,
  with mandatory expiry, refresh, revocation, and CSRF protections.
- Basic Auth username MUST map to `correo_electronico` persisted in `empleado`.
- Basic Auth password MUST be transient input and MUST be validated by comparing
  derived hash against persisted `contrasena_hash`.
- Authorization MUST enforce `USER` read-only access and `ADMIN` full CRUD access.
- Access matrix MUST be enforced consistently across all employee endpoints:
	`ADMIN`=CRUD and `USER`=read-only.
- The `empleado` table MUST require `correo_electronico` and `contrasena_hash`
  attributes.
- `contrasena` MUST be input-only and MUST NOT persist in plaintext.
- Public API routes MUST be major-versioned (`/api/v{major}`).
- Public contract expansion or new public endpoints MUST force API major increment.
- Official API major for features including `Departamento` MUST be `v3` or newer.
- Deprecated API versions past sunset MUST return `410 Gone` under UTC time.
- List endpoints MUST define default and maximum pagination limits.
- `departamento` persistence MUST exist as first-class entity when that domain is in
	scope, with explicit FK from `empleado` to `departamento` when assigned.
- `empleado` to `departamento` association MUST be nullable at creation/update but
	MUST be referentially valid when present.
- Implicit inter-entity joins without declared FK are forbidden.
- Official frontend stack MUST be Angular 22 LTS + TypeScript.
- Node version management for frontend MUST use nvm.
- Frontend MUST consume only official API endpoints and delegate authentication
	to API flows.
- Frontend MUST NOT duplicate backend business rules.
- Frontend E2E testing MUST use Cypress and cover login success, login failure,
	employee rendering, department rendering, and CRUD operations.
- A build is invalid if mandatory Cypress E2E tests do not pass.
- Frontend release versioning MUST remain independent from backend versioning.
- Development workflow MUST use feature branches and PR-based integration.

## Delivery Workflow and Quality Gates

- Each plan and implementation MUST include an explicit constitution compliance check.
- Pull requests MUST document impact on security, data model, and API documentation.
- Pull requests MUST document role-access impacts (`USER` read-only, `ADMIN` CRUD)
  when endpoint permissions change.
- Pull requests MUST include evidence that the role matrix is enforced (`ADMIN`=CRUD,
	`USER`=read-only) in tests for affected endpoints.
- Pull requests that modify employee persistence MUST show schema and validation
	evidence for required `correo_electronico` and `contrasena_hash` attributes,
	plus proof that `contrasena` is not persisted in plaintext.
- Pull requests that modify authentication MUST show Basic Auth evidence for all
	affected methods, including `username=correo_electronico` mapping and
	hash-comparison validation path.
- Pull requests that introduce JWT coexistence MUST provide evidence for token
  lifecycle controls (expiry, refresh, revocation), secure cookie policy,
  CSRF protections, and endpoint exposure classification.
- Pull requests MUST document API version and pagination impacts when endpoints change.
- Pull requests that include frontend changes MUST document Angular 22 LTS,
	TypeScript, nvm, Docker image/container updates, and Docker Compose integration.
- Pull requests that include frontend changes MUST include Cypress E2E evidence for:
	login success, login failure, employee rendering, department rendering, and CRUD.
- Pull requests that modify versioning MUST include evidence of sunset enforcement
	(`410 Gone`) and UTC-based cutoff behavior.
- Pull requests that introduce or modify `Departamento` domain behavior MUST include
  FK/migration evidence, nullable assignment behavior for `empleado`, and
  contract-level examples for the resulting major API version.
- A feature is not complete until HTTP Basic auth (email as username with
	hash-based password verification), role-based authorization,
	PostgreSQL (Docker), and Swagger evidence is present in code and documentation.
- Merges MUST include evidence of Git discipline: atomic commits and traceability to
	spec/tasks artifacts.
- Reviewers MUST block merges when constitutional requirements are missing.

## Governance

This constitution overrides conflicting local conventions for backend development
and automated development workflows. Amendments require: (1) documented proposal,
(2) impact summary on templates/process, and (3) version update under semantic versioning.

Versioning policy:
- MAJOR: incompatible governance or principle removals/redefinitions.
- MINOR: new principle/section or materially expanded mandatory guidance.
- PATCH: wording clarifications and non-semantic refinements.

Platform versioning policy:
- Backend API and official frontend versions are independently governed.
- Frontend SHOULD use the `vMAJOR.MINOR.PATCH-front` release tag format.

Agent Compliance:
- Automated implementation agents MUST adhere to Principle IX (Automated Agent Workflow
  Discipline) when executing feature implementations tied to spec identifiers.
- Violations of Principle IX (early abort, unasked branch creation, incomplete phases)
  MUST be corrected by explicit user instruction or agent re-initialization.
- Implementation progress MUST be tracked and visible to users via task lists or
  summary reports before phase transitions.

Compliance review policy:
- Every feature plan MUST pass a constitution gate before implementation starts.

- Every pull request MUST confirm compliance with all eleven principles, including the
	role policy (`USER` read-only, `ADMIN` CRUD), HTTP Basic auth semantics,
	secure password-hash persistence, required employee identity fields, and
	`Departamento` relational integrity rules.
- Violations MUST be tracked with rationale and remediation tasks before approval.

**Version**: 4.2.0 | **Ratified**: 2026-02-25 | **Last Amended**: 2026-03-19
