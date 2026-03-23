# Quickstart - Frontend Angular V1

## Prerequisites
- Docker + Docker Compose installed
- nvm installed
- Node version compatible with Angular 22 LTS (managed via nvm)
- Java 17 + Maven available for backend build when needed

## 1. Prepare frontend workspace
```bash
cd frontend
nvm use
npm ci
```

## 2. Build and run full stack with compose
```bash
cd ../docker
docker compose up -d --build
```

Expected services:
- `api` exposed on `http://localhost:8080`
- `postgres` exposed on `localhost:5433`
- `frontend` exposed on configured host port (e.g. `http://localhost:4200`)

## 3. Validate auth endpoints
```bash
curl -i -X POST http://localhost:8080/api/v4/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@empresa.com","password":"Admin123!"}'
```

Expected:
- `200 OK` with `{ status, role }` on valid credentials and auth tokens delivered via secure cookies
- `401 Unauthorized` with structured error on invalid credentials

## 4. Validate protected navigation behavior
- Open frontend URL.
- Attempt direct navigation to protected route without session.
- Verify redirect to login screen.

## 5. Run Cypress E2E gate
```bash
cd ../frontend
npm run test:e2e
```

Mandatory scenarios:
- Login valido
- Login invalido
- Render Empleados
- Render Departamentos
- CRUD basico
- `departamentos/{id}/empleados`

## 6. Verify refresh and logout
- Keep session active and force access token expiration in test environment.
- Trigger refresh flow and verify new token issuance.
- Execute logout and verify cookies invalidated + redirect to login.

## 7. Governance evidence
- Record in PR description the constitutional amendment evidence for Basic+JWT coexistence (Constitution v4.2.0, Principle II and Technical Constraints section).

## 8. Integration-test evidence matrix

| Scope | Evidence |
|-------|----------|
| Auth flows | `AuthV4ContractIT`, `AuthIntegrationIT` |
| Basic username/hash semantics | `BasicAuthUsernameMappingIntegrationIT`, `BasicAuthHashValidationIntegrationIT` |
| Role authorization | `SecurityCrudIntegrationIT` |
| DB/Flyway | `FlywayDepartamentosMigrationIT` + Testcontainers logs |
| API contracts | `AuthV4ContractIT`, `PaginationDefaultsContractIT` |

## 9. Traceability checklist

- Link each completed task ID to at least one commit hash.
- Include test execution evidence in PR (Maven + Cypress outputs).
- Include API contract diff when auth/domain contracts change.
