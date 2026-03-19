# Phase 0 Research - Frontend Angular V1

## Decision 1: Authentication model for frontend session
- Decision: Adopt JWT access token + refresh token model for frontend, while preserving existing Basic Auth for protected backend resources where currently enforced.
- Rationale: Avoids persisting user credentials in frontend storage and supports session continuity with controlled token renewal.
- Alternatives considered:
  - Keep only Basic Auth end-to-end: rejected due to frontend credential exposure risk and poor UX.
  - Basic for login validation only, then Basic for all API calls: rejected because credential persistence risk remains.

## Decision 2: Token storage and session persistence
- Decision: Store tokens in secure cookies (`HttpOnly`, `Secure`, `SameSite`), with access token session-scoped and refresh token persistent up to 12 hours.
- Rationale: Mitigates token exfiltration via JavaScript and aligns with agreed session persistence behavior.
- Alternatives considered:
  - localStorage/sessionStorage token persistence: rejected due to XSS blast radius.
  - Fully non-persistent tokens: rejected because it breaks requirement for persistence window.

## Decision 3: CSRF protection for cookie-based auth
- Decision: Require explicit anti-CSRF token validation for `/api/v4/auth/refresh`, `/api/v4/auth/logout`, and CRUD operations.
- Rationale: Cookie-based authentication without CSRF mitigation is vulnerable to cross-site request forgery.
- Alternatives considered:
  - SameSite-only protection: rejected as insufficient for all navigation/embedding scenarios.
  - CORS-only dependence: rejected because CORS does not replace CSRF controls.

## Decision 4: HTTP semantics for login endpoint
- Decision: `POST /api/v4/auth/login` returns `200 OK` with `{ token, role }` on success, `401 Unauthorized` with structured error on failure.
- Rationale: Standard auth semantics simplify client handling, observability, and automated test assertions.
- Alternatives considered:
  - Always `200` with status field: rejected due to ambiguous error handling.
  - `202/403` model: rejected as non-standard for credential failure path.

## Decision 5: API versioning scope
- Decision: Introduce auth endpoints under API major v4 (`/api/v4/auth/*`) while keeping domain endpoints in `/api/v3`.
- Rationale: New public endpoint surface requires major increment under constitutional policy.
- Alternatives considered:
  - Keep auth endpoints in v3: rejected due to constitutional conflict for new public endpoints.

## Decision 6: Frontend platform and test stack
- Decision: Use Angular 22 LTS + TypeScript + nvm, Cypress as mandatory E2E gate.
- Rationale: Matches constitution and feature constraints, with strong tooling for SPA and E2E validation.
- Alternatives considered:
  - React/Vue or Playwright-only: rejected by explicit constitutional/platform requirements.

## Decision 7: Docker and compose integration
- Decision: Add dedicated frontend Dockerfile and integrate `frontend` service into existing `docker/docker-compose.yml`.
- Rationale: Ensures reproducible local/CI runtime and aligns with project deployment model.
- Alternatives considered:
  - Run frontend outside compose: rejected due to environment drift risk.
