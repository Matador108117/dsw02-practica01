<!--
Sync Impact Report
- Version change: 1.0.0 -> 1.1.0
- Modified principles:
	- Template Principle 1 -> I. Backend Spring Standard
	- Template Principle 2 -> II. Basic Authentication Baseline
	- Template Principle 3 -> III. PostgreSQL + Docker by Default
	- Template Principle 4 -> IV. Contract-Driven API with Swagger
	- Template Principle 5 -> V. Testability and Operability Gates (expanded)
	- Added -> VI. API Versioning Compatibility
	- Added -> VII. API Pagination by Default
	- Added -> VIII. Git Workflow Discipline
- Added sections:
	- Technical Constraints
	- Delivery Workflow and Quality Gates
- Removed sections:
	- None
- Templates requiring updates:
	- ✅ updated: .specify/templates/plan-template.md
	- ✅ updated: .specify/templates/spec-template.md
	- ✅ updated: .specify/templates/tasks-template.md
	- ⚠ pending: .specify/templates/commands/*.md (directory not present)
	- ✅ verified: runtime guidance docs (README.md, docs/quickstart.md not present)
- Follow-up TODOs:
	- None
-->

# DSW02 Practica 01 Constitution

## Core Principles

### I. Backend Spring Standard
All backend services MUST be implemented with Spring Boot 3 and Java 17. New services,
modules, and examples MUST use this baseline stack and compatible dependencies only.
Rationale: a single modern stack reduces integration risk and maintenance cost.

### II. Basic Authentication Baseline
Protected endpoints MUST enforce HTTP Basic Authentication through Spring Security.
Authentication rules MUST be explicit in configuration and tested for authorized and
unauthorized access flows. Rationale: a clear baseline security control is mandatory.

### III. PostgreSQL + Docker by Default
Application persistence MUST target PostgreSQL. Local and CI execution MUST rely on
Docker-based database runtime (for example Docker Compose) to guarantee reproducible
environments. Rationale: consistent runtime parity avoids environment-specific defects.

### IV. Contract-Driven API with Swagger
HTTP APIs MUST be documented and exposed through OpenAPI/Swagger. Every delivered
endpoint MUST include request, response, and security documentation before release.
Rationale: accurate API contracts reduce onboarding and integration friction.

### V. Testability and Operability Gates
Changes MUST include automated tests proportional to risk, with mandatory integration
coverage for authentication, database access, and API contracts. Services MUST emit
structured logs for critical flows and startup/runtime failures. Rationale: reliability
depends on fast feedback and diagnosable production behavior.

### VI. API Versioning Compatibility
Public HTTP endpoints MUST be versioned in the URI path using the `/api/v{major}`
pattern. Backward-incompatible API changes MUST increment the major version and MUST
be documented in OpenAPI plus migration notes in feature specs. Backward-compatible
changes MUST preserve existing version behavior. Rationale: explicit API versioning
protects clients from accidental breaking changes.

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

## Technical Constraints

- Runtime MUST be Java 17.
- Framework MUST be Spring Boot 3.
- Persistence MUST be PostgreSQL.
- Local/CI database runtime MUST be Docker-managed.
- API documentation MUST be available via Swagger UI.
- Security for protected routes MUST use HTTP Basic Authentication.
- Public API routes MUST be major-versioned (`/api/v{major}`).
- List endpoints MUST define default and maximum pagination limits.
- Development workflow MUST use feature branches and PR-based integration.

## Delivery Workflow and Quality Gates

- Each plan and implementation MUST include an explicit constitution compliance check.
- Pull requests MUST document impact on security, data model, and API documentation.
- Pull requests MUST document API version and pagination impacts when endpoints change.
- A feature is not complete until Basic Auth, PostgreSQL (Docker), and Swagger evidence
	is present in code and documentation.
- Merges MUST include evidence of Git discipline: atomic commits and traceability to
	spec/tasks artifacts.
- Reviewers MUST block merges when constitutional requirements are missing.

## Governance

This constitution overrides conflicting local conventions for backend development.
Amendments require: (1) documented proposal, (2) impact summary on templates/process,
and (3) version update under semantic versioning.

Versioning policy:
- MAJOR: incompatible governance or principle removals/redefinitions.
- MINOR: new principle/section or materially expanded mandatory guidance.
- PATCH: wording clarifications and non-semantic refinements.

Compliance review policy:
- Every feature plan MUST pass a constitution gate before implementation starts.
- Every pull request MUST confirm compliance with all eight principles.
- Violations MUST be tracked with rationale and remediation tasks before approval.

**Version**: 1.1.0 | **Ratified**: 2026-02-25 | **Last Amended**: 2026-03-04
