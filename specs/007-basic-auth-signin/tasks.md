# Tasks: HTTP Basic Authentication with Email-Password Login

**Feature Branch**: `007-basic-auth-signin`  
**Status**: Implementation Ready  
**Last Updated**: 2026-03-17  
**Spec**: `/specs/007-basic-auth-signin/spec.md`

---

## Summary

This feature implements HTTP Basic Authentication for the Empleados API. Clients send email credentials via `Authorization: Basic` header; the system authenticates by decoding, searching empleado by email, and comparing password hash using constant-time comparison. All authentication flows are **stateless** (no session state), with per-email rate limiting (5 failed/min → 5–10 min lockout), strict fail-secure semantics (HTTP 503 on service failure), and comprehensive OpenAPI/Swagger documentation.

**Implementation Scope**:
- ✅ Spring Security HTTP Basic configuration
- ✅ Custom UserDetailsService for email-based authentication
- ✅ Rate-limiting filter with per-email brute-force defense
- ✅ Audit logging for authentication attempts
- ✅ OpenAPI/Swagger HTTP Basic security scheme documentation
- ❌ Password reset endpoints (deferred to Admin Dashboard)
- ❌ Account unlock endpoints (deferred to Admin Dashboard)
- ❌ New database migrations (reuse existing `correo_electronico` and `contrasena_hash`)

**Quality Gates**:
- All 52 tasks statused as "done" or "pending merge review"
- All integration tests pass (auth success/failure, rate-limiting, service failures)
- Swagger UI correctly displays HTTP Basic security scheme with "Authorize" button
- OpenAPI contract validation passes
- Audit logs capture all auth attempts with 90-day retention capability
- No plaintext passwords in logs or error responses
- Constant-time hash comparison verified via code review
- Code coverage ≥80% for auth-related classes
- PR merged to main branch; feature tagged v2.0.0

---

## Table of Contents

1. [Task List with Checklist Format](#task-list-with-checklist-format)
2. [Effort Estimates & Metrics](#effort-estimates--metrics)
3. [Task Dependencies & Ordering](#task-dependencies--ordering)
4. [Parallel Execution Examples](#parallel-execution-examples)
5. [User Story Acceptance Criteria](#user-story-acceptance-criteria)
6. [Risk Mitigation & Known Issues](#risk-mitigation--known-issues)
7. [Implementation Strategy](#implementation-strategy)

---

## Task List with Checklist Format

### Phase 1: Infrastructure & Setup (Blocking Prerequisites)

- [ ] T001 Create project structure and branch checkpoint
- [ ] T002 Create Spring Security configuration file: `src/main/java/com/dsw02/empleados/config/SecurityConfig.java`
- [ ] T003 Create EmpleadoUserDetailsService class: `src/main/java/com/dsw02/empleados/service/EmpleadoUserDetailsService.java`
- [ ] T004 Create RateLimitService class: `src/main/java/com/dsw02/empleados/service/RateLimitService.java`
- [ ] T005 Create RateLimitFilter class: `src/main/java/com/dsw02/empleados/filter/RateLimitFilter.java`
- [ ] T006 Create AuthAuditFilter class: `src/main/java/com/dsw02/empleados/filter/AuthAuditFilter.java`
- [ ] T007 [P] Update `src/main/resources/application.yml` with rate-limit configuration
- [ ] T008 [P] Create/update `src/main/resources/logback.xml` for audit logging configuration
- [ ] T009 Verify no new Flyway migrations needed; confirm reuse of existing `correo_electronico` and `contrasena_hash` columns
- [ ] T010 Create unit test base class: `src/test/java/com/dsw02/empleados/AuthTestFixtures.java`

### Phase 2: Spring Security Core Configuration

- [ ] T011 [US1] Configure HttpBasic authentication in SecurityConfig.java per Spring Security 6.x standards
- [ ] T012 [US1] Configure PasswordEncoder bean with bcrypt as default (work factor 12; Argon2 optional):
  - [ ] T012a: Implement BCryptPasswordEncoder(12) as primary PasswordEncoder bean
  - [ ] T012b: Document choice in SecurityConfig comment: "Bcrypt chosen for lower CPU overhead in internal API; Argon2 available as future upgrade"
  - [ ] T012c: Test password validation with bcrypt hashes containing each cost factor (10, 12, 14)
- [ ] T013 [US1] Register EmpleadoUserDetailsService as UserDetailsService bean
- [ ] T014 [US1] Configure SecurityFilterChain to enforce HTTP Basic on `/api/v2/**` endpoints
- [ ] T015 [US1] Exclude public endpoints (health checks, actuator) from authentication requirement
- [ ] T016 [US2] Configure HttpBasic challenge response with `WWW-Authenticate: Basic realm="empleados-api"` header
- [ ] T017 [US2] Configure authentication exception handlers (AuthenticationException → HTTP 401)
- [ ] T018 [US2] Configure authorization exception handlers (AccessDeniedException → HTTP 403)
- [ ] T019 [P] [US1] Implement role-based access control: @PreAuthorize for ADMIN and USER roles
- [ ] T020 [P] [US1] Implement method-level security decorators on EmpleadoService/EmpleadoController

### Phase 3: Service Layer - Authentication Logic

- [ ] T021 [US1] Implement EmpleadoUserDetailsService.loadUserByUsername(email) to:
  - [ ] T021a: Query `empleado` table by `correo_electronico` (case-insensitive per RFC 5321; use PostgreSQL LOWER() or Java equalsIgnoreCase() with UTF-8 support for accented characters)
  - [ ] T021b: Throw UsernameNotFoundException if correo_electronico not found (return generic error, don't leak email existence)
  - [ ] T021c: Load Empleado entity with `activo=true` check
  - [ ] T021d: Build UserDetails from Empleado with roles (USER, ADMIN)
  - [ ] T021e: Return UserDetails with `contrasena_hash` for PasswordEncoder validation
- [ ] T022 [US1] Implement PasswordEncoder.matches() verification with constant-time comparison check
- [ ] T023 [P] [US1] Implement EmpleadoUserDetailsService.loadUserByUsername error handling:
  - [ ] T023a: Catch database exceptions (DataAccessException); don't expose SQL errors to client
  - [ ] T023b: Log database failures securely (exclude credentials)
  - [ ] T023c: Return HTTP 503 on database unavailable
- [ ] T024 Create EmpleadoUserDetailsService.createUserDetails() helper method
- [ ] T025 Implement EmpleadoUserDetailsService.buildGrantedAuthorities() to map Empleado roles → Spring roles

### Phase 4: Rate-Limiting & Brute-Force Defense

- [ ] T026 [US2] Implement RateLimitService.recordFailedAttempt(email) to:
  - [ ] T026a: Increment failed attempt count for email in current window
  - [ ] T026b: Check if count >= 5; if so, mark email as locked with exponential backoff
  - [ ] T026c: Calculate backoff duration (5 min for 1st lockout, 10 min for 2nd, etc.)
  - [ ] T026d: Persist lockout end timestamp in in-memory ConcurrentHashMap
- [ ] T027 [US2] Implement RateLimitService.recordSuccessfulAttempt(email) to reset failed count
- [ ] T028 [US2] Implement RateLimitService.isRateLimited(email) check
- [ ] T029 [US2] Implement RateLimitService.cleanup() background thread:
  - [ ] T029a: Run every 5 minutes to remove expired rate-limit entries
  - [ ] T029b: Prevent unbounded memory growth of ConcurrentHashMap
- [ ] T030 [US2] Implement RateLimitFilter.doFilter() to:
  - [ ] T030a: Extract email from Authorization header before password validation
  - [ ] T030b: Check isRateLimited(email) before attempting authentication
  - [ ] T030c: Return HTTP 429 (Too Many Requests) if rate-limited; include Retry-After header
  - [ ] T030d: Call recordFailedAttempt() after auth failure
  - [ ] T030e: Call recordSuccessfulAttempt() after auth success
- [ ] T031 [US2] Implement rate-limit response payload: `{ "error": "Too Many Requests", "message": "Rate limit exceeded; try again in X minutes" }`
- [ ] T032 [P] Register RateLimitFilter in SecurityFilterChain before BasicAuthenticationFilter
- [ ] T033 [P] Configure rate-limit parameters in application.yml (max attempts, window, backoff durations)

### Phase 5: Audit Logging & Compliance

- [ ] T034 [US1] Implement AuthAuditFilter.doFilter() to log all authentication attempts:
  - [ ] T034a: Log email (anonymized option: hash or last 2 chars only)
  - [ ] T034b: Log timestamp of attempt
  - [ ] T034c: Log result: "SUCCESS" or "FAILURE" (never include password or hash)
  - [ ] T034d: Log failure reason (e.g., "user_not_found", "invalid_password", "rate_limited")
  - [ ] T034e: Exclude Authorization header from request logs
- [ ] T035 [P] [US1] Update logback.xml to:
  - [ ] T035a: Define separate audit appender for authentication logs
  - [ ] T035b: Set retention to 90 days per compliance requirement
  - [ ] T035c: Mask sensitive patterns (Authorization header, passwords)
- [ ] T036 [P] Register AuthAuditFilter in SecurityFilterChain after RateLimitFilter
- [ ] T037 Verify audit logs do NOT contain plaintext passwords or Authorization header values
- [ ] T038 Create SecurityContext principal initialization in EmpleadoUserDetailsService:
  - [ ] T038a: Set current Empleado ID in Security principal for audit trail
  - [ ] T038b: Update `fecha_ultima_activ` timestamp on success

### Phase 6: OpenAPI & Swagger Documentation

- [ ] T039 [US3] Update OpenAPI spec (contracts/empleados-auth-v2.openapi.yaml) to include:
  - [ ] T039a: Global security scheme definition: `type: http, scheme: basic, realm: empleados-api`
  - [ ] T039b: Request example with Authorization header
  - [ ] T039c: Response examples (HTTP 200 success, HTTP 401 invalid credentials, HTTP 429 rate-limited, HTTP 503 service unavailable)
- [ ] T040 [US3] Add OpenAPI security requirement to all protected endpoints (all except health/actuator)
- [ ] T041 [US3] Configure springdoc-openapi to render HTTP Basic scheme in Swagger UI:
  - [ ] T041a: Enable "Authorize" button in Swagger UI
  - [ ] T041b: Display HTTP Basic prompt form (email + password fields)
- [ ] T042 [US3] Test Swagger UI "Authorize" button functionality manually
- [ ] T043 [US3] Validate OpenAPI spec against OpenAPI 3.0 schema validator

### Phase 7: Integration Tests - Authentication Success Flow (US1)

- [ ] T044 [US1] Create AuthenticationIntegrationTest.java:
  - [ ] T044a: Test valid admin@example.com credential flow
  - [ ] T044b: Test valid user@example.com credential flow
  - [ ] T044c: Verify HTTP 200 response from protected endpoint
  - [ ] T044d: Verify SecurityContext contains correct Empleado ID and roles
  - [ ] T044e: Verify `fecha_ultima_activ` is updated on successful auth
  - [ ] T044f: Test with special characters in email (e.g., user+tag@example.com)
- [ ] T045 [US1] [P] Create AuthorizedIntegrationTest.java for role-based access:
  - [ ] T045a: Test ADMIN role can POST (create), PUT (update), DELETE empleados
  - [ ] T045b: Test USER role can only GET (list/read) empleados
  - [ ] T045c: Test USER role receives HTTP 403 Forbidden on POST/PUT/DELETE attempts
  - [ ] T045d: Test authorization is independent of authentication method

### Phase 8: Integration Tests - Authentication Failure Flow (US2)

- [ ] T046 [US2] Create InvalidCredentialsIntegrationTest.java:
  - [ ] T046a: Test missing Authorization header → HTTP 401 + WWW-Authenticate header
  - [ ] T046b: Test malformed Authorization header (invalid base64) → HTTP 401
  - [ ] T046c: Test missing colon separator in decoded credentials → HTTP 401
  - [ ] T046d: Test non-existent email → HTTP 401 (generic error, no email existence leak)
  - [ ] T046e: Test correct email, wrong password → HTTP 401 (generic error, no password details)
  - [ ] T046f: Test case-sensitivity of email (both lowercase and mixed case should work)
- [ ] T047 [US2] [P] Verify no plaintext password in HTTP 401 error response payload
- [ ] T048 [US2] [P] Verify error messages do NOT help attacker distinguish email existence from password mismatch

### Phase 9: Integration Tests - Rate-Limiting & Brute-Force Defense (US2)

- [ ] T049 [US2] Create RateLimitIntegrationTest.java:
  - [ ] T049a: Execute 5 failed login attempts for same correo_electronico within 1 minute
  - [ ] T049b: Verify 5th attempt still returns HTTP 401, not HTTP 429
  - [ ] T049c: Verify 6th attempt within window returns HTTP 429 Too Many Requests
  - [ ] T049d: Verify 60-second window resets after 1 minute; retry succeeds after cooldown
  - [ ] T049e: Verify successful login resets failed attempt counter for that correo_electronico
  - [ ] T049f: Test exponential backoff: 1st lockout 5 min, 2nd lockout 10 min
  - [ ] T049g: Test concurrent requests from different correo_electronico values: each has independent counter
  - [ ] T049h: Test concurrent requests from same correo_electronico during rate-limit window
  - [ ] T049i: **(Finding A1) UTF-8 Test**: Test case-insensitive correo_electronico lookup with accented characters (josé@example.com, müller@example.com, joão@example.com); verify both lowercase/uppercase variants authenticate; verify rate-limit counter is correctly scoped per RFC 5321

### Phase 10: Integration Tests - Service Failure & Fail-Secure Handling (US1, US2)

- [ ] T050 [US1] Create FailureHandlingIntegrationTest.java:
  - [ ] T050a: Simulate database unavailable (PostgreSQL down)
  - [ ] T050b: Verify HTTP 503 Service Unavailable returned within 1 second
  - [ ] T050c: Verify NO fallback/bypass mechanism (fail-secure)
  - [ ] T050d: Verify NO error details in response (generic "Service currently unavailable")
  - [ ] T050e: Test recovery: restart database, verify auth resumes normally
- [ ] T051 [P] [US2] Test rate-limit service failures (e.g., in-memory map corruption)
  - [ ] T051a: Verify system remains secure (fails to rate-limit → deny, not allow)
  - [ ] T051b: Log rate-limit service failures for ops team

### Phase 11: Contract & Documentation Tests

- [ ] T052 Create ContractIT.java to validate OpenAPI spec adherence:
  - [ ] T052a: Parse OpenAPI spec from contracts/empleados-auth-v2.openapi.yaml
  - [ ] T052b: Verify security scheme is `type: http, scheme: basic`
  - [ ] T052c: Verify all protected endpoints include security requirement
  - [ ] T052d: Verify HTTP 401, HTTP 403, HTTP 429, HTTP 503 responses documented
  - [ ] T052e: Validate response schemas match implementation

### Phase 12: Unit Tests - EmpleadoUserDetailsService

- [ ] T053 Create EmpleadoUserDetailsServiceTest.java:
  - [ ] T053a: Test loadUserByUsername with existing empleado
  - [ ] T053b: Test loadUserByUsername returns correct roles (USER vs ADMIN)
  - [ ] T053c: Test loadUserByUsername with non-existent email → UsernameNotFoundException
  - [ ] T053d: Test loadUserByUsername with `activo=false` empleado → rejected
  - [ ] T053e: Test loadUserByUsername with special characters in email
  - [ ] T053f: Test case-insensitive email lookup
  - [ ] T053g: Test database exception handling (DataAccessException → generic error)

### Phase 13: Unit Tests - RateLimitService

- [ ] T054 Create RateLimitServiceTest.java:
  - [ ] T054a: Test recordFailedAttempt increments counter
  - [ ] T054b: Test counter resets after window expiry
  - [ ] T054c: Test isRateLimited returns false for count < 5
  - [ ] T054d: Test isRateLimited returns true for count >= 5 and lockout not expired
  - [ ] T054e: Test recordSuccessfulAttempt resets counter
  - [ ] T054f: Test exponential backoff calculation
  - [ ] T054g: Test concurrent access (multi-threaded) doesn't corrupt counter
  - [ ] T054h: Test cleanup removes expired entries

### Phase 14: Unit Tests - SecurityConfig & Filters

- [ ] T055 Create SecurityConfigTest.java:
  - [ ] T055a: Test SecurityFilterChain configuration loads correctly
  - [ ] T055b: Test PasswordEncoder bean is bcrypt/Argon2 (not plaintext)
  - [ ] T055c: Test HttpBasic authentication is enabled
  - [ ] T055d: Test RateLimitFilter is registered before BasicAuthenticationFilter
  - [ ] T055e: Test public endpoints (health, actuator) bypass authentication
- [ ] T056 [P] Create RateLimitFilterTest.java:
  - [ ] T056a: Test doFilter logic for rate-limited request
  - [ ] T056b: Test doFilter calls recordFailedAttempt on auth failure
  - [ ] T056c: Test doFilter calls recordSuccessfulAttempt on auth success
- [ ] T057 [P] Create AuthAuditFilterTest.java:
  - [ ] T057a: Test audit log contains email and timestamp
  - [ ] T057b: Test audit log does NOT contain Authorization header
  - [ ] T057c: Test audit log does NOT contain password or hash

### Phase 15: Code Quality & Coverage

- [ ] T058 [P] Run SonarQube or equivalent static analysis:
  - [ ] T058a: Verify no hardcoded credentials
  - [ ] T058b: Verify no SQL injection vulnerabilities
  - [ ] T058c: Verify no logging of sensitive data
  - [ ] T058d: Code coverage >= 80% for auth-related classes
- [ ] T059 [P] Review constant-time hash comparison in PasswordEncoder:
  - [ ] T059a: Confirm Spring Security bcrypt PasswordEncoder.matches() uses constant-time comparison
  - [ ] T059b: Verify no custom hash comparison code introduced timing vulnerability
- [ ] T060 [P] Review all date/timestamp handling for UTC consistency
- [ ] T061 Review audit log rotation and retention policy (90-day minimum)

### Phase 16: Documentation & Deployment

- [ ] T062 Update feature branch README.md with HTTP Basic setup guide:
  - [ ] T062a: Include credentials for test users (admin@example.com, user@example.com)
  - [ ] T062b: Include rate-limit configuration details
  - [ ] T062c: Include audit logging retention policy
  - [ ] T062d: Include HTTPS requirement for production
- [ ] T063 [P] Create migration guide for existing API clients:
  - [ ] T063a: Document how to add Authorization header to requests
  - [ ] T063b: Document how to handle HTTP 401, HTTP 429, HTTP 503 responses
- [ ] T064 [P] Create operations runbook:
  - [ ] T064a: Monitoring alerts for authentication failures
  - [ ] T064b: Alert thresholds (e.g., >10 failures/min across all users)
  - [ ] T064c: Rate-limit lockout procedures
  - [ ] T064d: Emergency credential rollover procedures
- [ ] T065 Create CHANGELOG entry for v2.0.0 release

### Phase 17: Final Validation & Merge

- [ ] T066 Run full integration test suite end-to-end
- [ ] T067 Verify Swagger UI "Authorize" button works with test credentials
- [ ] T068 Verify OpenAPI spec is valid per OpenAPI 3.0.0 spec
- [ ] T069 Verify Docker Compose orchestration includes PostgreSQL and API
- [ ] T070 Create PR with atomic commits referencing spec/tasks
- [ ] T071 Obtain code review sign-off from security and backend leads
- [ ] T072 Merge PR to main branch with --no-ff flag
- [ ] T073 Tag release as v2.0.0 with commit message referencing spec/007
- [ ] T074 Deploy to staging environment and smoke-test
- [ ] T075 Monitor production deployment for auth errors (first 1 hour)

---

## Effort Estimates & Metrics

### T-Shirt Sizing

| Size | Duration | Tasks |
|------|----------|-------|
| **XS** | 15 min | T001, T009, T015, T042, T043, T059, T061, T065, T069, T073 |
| **S** | 1 hr | T002, T003, T004, T005, T006, T007, T008, T010, T011, T012, T013, T014, T016, T017, T018, T024, T025, T032, T035, T037, T047, T048, T051, T055, T056, T057, T062, T063, T064 |
| **M** | 4 hr | T019, T020, T021, T022, T023, T026, T027, T028, T030, T031, T033, T034, T036, T038, T039, T040, T041, T049, T050, T052, T053, T054, T058, T060, T066, T067, T068, T070, T074 |
| **L** | 8 hr | T029, T044, T045, T046, T071 |
| **XL** | 16 hr+ | T075 (ongoing monitoring) |

### Metrics Summary

- **Total Tasks**: 75 (breakdown below)
- **Parallelizable Tasks ([P])**: 23
- **User Story 1 (US1) Tasks**: 18
- **User Story 2 (US2) Tasks**: 16
- **User Story 3 (US3) Tasks**: 5
- **Blocking/Infrastructure Tasks**: 36

### Estimated Total Effort

- **Sequential Path** (critical path): ~80 hours (5 weeks @ 16 hrs/week)
- **With Parallel Execution** (recommended): ~60 hours (3.75 weeks @ 16 hrs/week)
  - Infrastructure + SecurityConfig: ~20 hours
  - Service Layer + Rate-Limiting (parallel): ~16 hours
  - Tests (parallel): ~24 hours

---

## Task Dependencies & Ordering

### Dependency Graph

```
T001 (Setup)
├─ T002-T010 (Infrastructure) [BLOCKING]
│  ├─ T011-T020 (Spring Security Core) [REQUIRES T001-T010]
│  │  └─ T021-T025 (Service Layer - Auth Logic) [REQUIRES T011-T020]
│  │     └─ T026-T033 (Rate-Limiting) [PARALLEL with T021-T025]
│  │        └─ T034-T038 (Audit Logging) [REQUIRES T026-T033]
│  │           └─ T039-T043 (OpenAPI/Swagger) [REQUIRES T034-T038]
│  │              └─ T044-T075 (Tests + Validation) [REQUIRES T039-T043]

Parallelizable Paths (after T001-T010):
├─ Path A (Testing): T044-T057 can run in parallel with T021-T043
├─ Path B (Documentation): T062-T064 can run after T039
└─ Path C (Code Quality): T058-T061 can run after T055-T057
```

### Critical Path (Minimum Blocking Sequence)

1. **T001-T010**: Infrastructure setup (10 hrs)
2. **T011-T020**: Spring Security configuration (15 hrs)
3. **T021-T025**: Core authentication service (12 hrs)
4. **T026-T033**: Rate-limiting implementation (10 hrs)
5. **T034-T038**: Audit logging (8 hrs)
6. **T039-T043**: OpenAPI documentation (6 hrs)
7. **T066-T073**: Validation & merge (8 hrs)
8. **Total Critical Path**: ~69 hours

### Parallel Acceleration Opportunities

After completing T020, these can run in parallel:
- **Test Suite (T044-T057)**: ~24 hrs (in parallel with T021-T038)
- **Documentation (T062-T064)**: ~4 hrs (in parallel with T021-T038)
- **Code Quality (T058-T061)**: ~3 hrs (in parallel with T055-T057)

**Recommended Schedule**:
- **Week 1**: T001-T020 (sequential, ~25 hrs)
- **Week 2-3**: T021-T057 (parallel: service layer + tests, ~35 hrs total)
- **Week 4**: T058-T075 (final validation, documentation, merge, ~12 hrs)
- **Total**: ~72 hours (4.5 weeks @ 16 hrs/week)

---

## Parallel Execution Examples

### Parallel Set 1: Spring Security Setup (After T010)

These tasks have no inter-dependencies and can be executed by different team members simultaneously:

```
Executor A:
- T011: Configure HttpBasic authentication
- T014: Configure SecurityFilterChain
- T017: Configure authentication exception handlers

Executor B (parallel):
- T012: Configure PasswordEncoder bean
- T013: Register UserDetailsService
- T019: Implement role-based access control

Executor C (parallel):
- T015: Exclude public endpoints
- T016: Configure challenge response header
- T020: Method-level security decorators
```

**Duration**: 1 day (instead of 3 days sequential)  
**Dependency**: All must complete before T021

---

### Parallel Set 2: Service Layer Implementation (After T020)

```
Executor A (Auth Logic):
- T021: Implement EmpleadoUserDetailsService
- T022: Implement password validation
- T024: Helper method for UserDetails
- T025: buildGrantedAuthorities()

Executor B (Rate-Limiting, parallel):
- T026: recordFailedAttempt()
- T027: recordSuccessfulAttempt()
- T028: isRateLimited check
- T029: cleanup() background thread
- T030: RateLimitFilter.doFilter()

Executor C (Audit, parallel):
- T034: AuthAuditFilter.doFilter()
- T035: logback.xml configuration
- T036: Register AuthAuditFilter
```

**Dependencies**: Executor B requires T012 (PasswordEncoder). Executor C requires T034 (no prior blocking).  
**Duration**: 1.5 days (instead of 3.5 days sequential)

---

### Parallel Set 3: Integration Test Suite (After T039)

```
Executor A (Success/Auth Tests):
- T044: AuthenticationIntegrationTest
- T045: AuthorizedIntegrationTest

Executor B (Failure Tests, parallel):
- T046: InvalidCredentialsIntegrationTest
- T049: RateLimitIntegrationTest

Executor C (Failure Handling, parallel):
- T050: FailureHandlingIntegrationTest
- T052: ContractIT

Executor D (Unit Tests, parallel):
- T053: EmpleadoUserDetailsServiceTest
- T054: RateLimitServiceTest
- T055: SecurityConfigTest
- T056: RateLimitFilterTest
- T057: AuthAuditFilterTest
```

**Duration**: 1 day (instead of 4 days sequential)  
**All tests can run in parallel after T039**

---

## User Story Acceptance Criteria

### User Story 1: Authenticate with Valid Email and Password (P1, Tasks: 18)

**Definition of Done**:
- All 18 US1-tagged tasks completed and verified
- All US1 integration tests passing
- Code coverage >= 80% for EmpleadoUserDetailsService, SecurityConfig, PasswordEncoder logic

**Acceptance Criteria** (from spec.md, mapped to tasks):

| Criterion | Tasks | Test |
|-----------|-------|------|
| **AC1**: Valid email:password credential decoding | T021a, T022 | T044a, T053a |
| **AC2**: Database lookup by `correo_electronico` | T021a, T021c | T044a, T053a, T053b |
| **AC3**: Constant-time hash comparison | T022, T059b | T044a, T055d |
| **AC4**: HTTP 200 success response | T044a, T044c | T044c |
| **AC5**: SecurityContext contains Empleado ID + roles | T021d, T025 | T044d |
| **AC6**: `fecha_ultima_activ` updated on success | T038, T044e | T044e |
| **AC7**: Special characters in email handled (RFC 5321) | T021a, T023a | T044f, T053e |
| **AC8**: Email case-insensitive for lookup | T021a, T046f | T053f, T046f |
| **AC9**: Stateless (no session state) | T011, T014, T015 | T044b (subsequent requests) |
| **AC10**: HTTPS requirement documented | T062, T063 | Manual review |

**Test Success Metrics**:
- ✅ AuthenticationIntegrationTest.testValidAdminCredential() passes
- ✅ AuthenticationIntegrationTest.testValidUserCredential() passes
- ✅ AuthenticationIntegrationTest.testSpecialCharactersInEmail() passes
- ✅ AuthenticationIntegrationTest.testStatelessAuthPerRequest() passes
- ✅ AuthorizedIntegrationTest.testAdminCanCRUD() passes
- ✅ AuthorizedIntegrationTest.testUserCanReadonly() passes

---

### User Story 2: Reject Invalid Credentials with Proper Error Response (P1, Tasks: 16)

**Definition of Done**:
- All 16 US2-tagged tasks completed and verified
- All US2 integration tests passing (InvalidCredentials, RateLimit, FailureHandling)
- Code coverage >= 80% for RateLimitService, RateLimitFilter, auth exception handling
- No plaintext password leakage in error responses or logs

**Acceptance Criteria** (from spec.md, mapped to tasks):

| Criterion | Tasks | Test |
|-----------|-------|------|
| **AC1**: Missing Authorization header → HTTP 401 | T017, T046a | T046a |
| **AC2**: Malformed Base64 → HTTP 401 | T017, T046c | T046c |
| **AC3**: Non-existent email → HTTP 401 (no email leak) | T021a, T046d | T046d, T053c |
| **AC4**: Wrong password → HTTP 401 (no password hint) | T022, T046e | T046e |
| **AC5**: WWW-Authenticate header included | T016, T046a | T046a |
| **AC6**: Rate-limit: 5 fails/min → HTTP 429 lockout | T026, T030, T049c | T049c, T049d |
| **AC7**: Rate-limit window resets after 60 seconds | T026, T029, T049d | T049d |
| **AC8**: Successful auth resets failed counter | T027, T049e | T049e |
| **AC9**: Exponential backoff (5 min → 10 min) | T026c, T049f | T049f |
| **AC10**: No plaintext password in error response | T047 | T046a, T046e, T047 |
| **AC11**: No plaintext password in logs | T034c, T035c, T037 | T057c |
| **AC12**: Concurrent requests: independent counters | T049g | T049g |
| **AC13**: Service failure → HTTP 503 (fail-secure) | T023c, T050b | T050b |
| **AC14**: No fallback/bypass on failure | T050c | T050c |
| **AC15**: Error logged with timestamp (audit trail) | T034a, T034b | T050e (audit log review) |
| **AC16**: Rate-limit includes Retry-After header | T030c | T049c (response header validation) |

**Test Success Metrics**:
- ✅ InvalidCredentialsIntegrationTest.testMissingAuthHeader() passes
- ✅ InvalidCredentialsIntegrationTest.testNonexistentEmail() passes
- ✅ InvalidCredentialsIntegrationTest.testWrongPassword() passes
- ✅ InvalidCredentialsIntegrationTest.testNoPasswordLeakage() passes
- ✅ RateLimitIntegrationTest.testFiveFailuresPerMinute() passes
- ✅ RateLimitIntegrationTest.testHTTP429AfterLockout() passes
- ✅ RateLimitIntegrationTest.testExponentialBackoff() passes
- ✅ FailureHandlingIntegrationTest.testDatabaseUnavailable() → HTTP 503
- ✅ FailureHandlingIntegrationTest.testFailSecureNoBypass() passes
- ✅ AuthAuditFilterTest.testNoPasswordInAuditLog() passes

---

### User Story 3: Document Authentication in Swagger UI (P2, Tasks: 5)

**Definition of Done**:
- All 5 US3-tagged tasks completed and verified
- Swagger UI "Authorize" button renders and functions correctly
- OpenAPI spec validated against OpenAPI 3.0 schema
- Swagger UI accessible at `http://localhost:8080/swagger-ui.html`

**Acceptance Criteria** (from spec.md, mapped to tasks):

| Criterion | Tasks | Test |
|-----------|-------|------|
| **AC1**: OpenAPI global security scheme defined | T039a | T052b |
| **AC2**: HTTP Basic scheme type=http, scheme=basic | T039a, T041 | T052b |
| **AC3**: "Authorize" button visible in Swagger UI | T041a, T042 | Manual: browse Swagger UI |
| **AC4**: Authorize dialog prompts for email + password | T041b, T042 | Manual: click Authorize button |
| **AC5**: Authorized requests include Authorization header | T041b, T042 | Manual: verify request headers in browser dev tools |
| **AC6**: Logout clears credentials | T042 | Manual: click Logout button |
| **AC7**: All protected endpoints marked `/api/v2/**` | T040 | T052d |
| **AC8**: HTTP 401, 429, 503 responses documented | T039c | T052d |
| **AC9**: Request/response examples provided | T039b, T039c | T052e |
| **AC10**: OpenAPI spec valid per schema | T043 | T052 (ContractIT) |

**Test Success Metrics**:
- ✅ Swagger UI loads at `http://localhost:8080/swagger-ui.html`
- ✅ "Authorize" button visible (top-right corner)
- ✅ Authorize dialog accepts email + password
- ✅ Authenticated requests include `Authorization: Basic <base64>` header
- ✅ OpenAPI spec validates against OpenAPI 3.0 schema
- ✅ ContractIT.testSecurityScheme() passes
- ✅ ContractIT.testAllProtectedEndpointsHaveSecurity() passes

---

## Risk Mitigation & Known Issues

### Risk 1: Timing Attack on Password Hash Comparison

**Risk**: Attacker measures response time to infer password validity; slower responses suggest longer match before failure.

**Mitigation**:
- ✅ Use Spring Security's PasswordEncoder.matches() which implements constant-time comparison
- ✅ Verify NO custom hash comparison code (code review gate at T059b)
- ✅ Task T022 explicitly calls PasswordEncoder.matches()
- ✅ Test: Time 1000 valid/invalid password attempts; verify variance < 5% (T044a jMH benchmark)

**Monitoring**: Alert if average response time for failed auth < 10ms (suggests early-exit without hash comparison)

---

### Risk 2: Rate-Limit Bypass via Concurrent Requests

**Risk**: Attacker sends 10 concurrent requests for same email; only 5 decrement before lockout, others bypass.

**Mitigation**:
- ✅ Use ConcurrentHashMap for thread-safe counter increment (T026a)
- ✅ Atomic operations: `counter.incrementAndGet()`, `counter.compareAndSet()`
- ✅ Test concurrent requests (T049h: 10 concurrent requests with same email)
- ✅ Verify counter reaches 5+ before any HTTP 429 response

**Implementation Detail**: Java ConcurrentHashMap provides atomic increment via AtomicInteger.

---

### Risk 3: Session State Leakage (Violates Stateless Semantics)

**Risk**: Developer mistakenly adds `HttpSecurity.sessionManagement()` creating session state, breaking HTTP Basic semantics.

**Mitigation**:
- ✅ SecurityConfig explicitly sets `.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))`
- ✅ Task T011 includes this setting
- ✅ Code review: any `Session` or `HttpSession` class import is red-flag
- ✅ Test T044b: verify subsequent requests to same endpoint use different SecurityContext (no session cache)

**Prevention**: Add assertion in SecurityConfigTest that `sessionCreationPolicy == STATELESS`

---

### Risk 4: Email Lookup Case-Sensitivity Issue

**Risk**: Database is case-sensitive for email; attacker uses `ADMIN@EXAMPLE.COM` vs `admin@example.com` to bypass.

**Mitigation**:
- ✅ Task T021a: use case-insensitive lookup: `EmpleadoRepository.findByCorreoElectronico(email.toLowerCase())`
- ✅ Database constraint (existing): `UNIQUE(LOWER(correo_electronico))` should be enforced
- ✅ Test T053f: verify case-insensitive lookup

**Implementation**: Spring Data JPA query with `LOWER()` function:
```java
Optional<Empleado> findByCorreoElectronicoCaseInsensitive(String email);
// OR
findByCorreoElectronico(email.toLowerCase())
```

---

### Risk 5: Database Unavailable → Fallback Authentication

**Risk**: Developer adds "offline mode" or cached credentials on database failure, breaking fail-secure.

**Mitigation**:
- ✅ Task T023c explicitly rejects this: "catch DataAccessException, return HTTP 503"
- ✅ Task T050: test database unavailable → HTTP 503 (no fallback)
- ✅ Code review: search for `if (database == null) { fallback }` patterns
- ✅ Specification constraint BC-018: "MUST NOT trigger fallback/bypass mechanisms"

**Architecture Rule**: RateLimitService + EmpleadoUserDetailsService should NOT implement caching or offline mode. If database unavailable, reject with HTTP 503.

---

### Risk 6: Rate-Limit Data Corruption Under Load

**Risk**: ConcurrentHashMap corruption under extreme concurrency (>10k/sec requests); counter becomes negative or inconsistent.

**Mitigation**:
- ✅ Use `ConcurrentHashMap<String, AtomicInteger>` for thread-safe operation (T026, T027, T028)
- ✅ Load test: 1000 concurrent requests/sec for 60 seconds (T049h extended)
- ✅ Verify counter never negative and consistent with actual attempts
- ✅ Monitor: alert if counter > 1000 (indicates memory leak)

**Implementation**: 
```java
ConcurrentHashMap<String, AtomicInteger> failedAttempts = new ConcurrentHashMap<>();
failedAttempts.computeIfAbsent(email, k -> new AtomicInteger(0)).incrementAndGet();
```

---

### Risk 7: Audit Log Plaintext Password Leakage

**Risk**: Misconfiguration logs `Authorization: Basic ...` header exposing credentials.

**Mitigation**:
- ✅ Task T035c: configure logback to MASK patterns matching `Authorization:`
- ✅ Task T037: verify audit logs do NOT contain Authorization header
- ✅ Task T057c: AuthAuditFilterTest verifies no credential leakage
- ✅ Static analysis: grep logs for `Authorization|password|contrasena` (T058a)

**Implementation**:
```xml
<!-- logback.xml -->
<pattern>%d{ISO8601} %level %logger - %replace(%msg){'Authorization: [^ ]*', 'Authorization: [redacted]'} %n</pattern>
```

---

### Risk 8: OpenAPI Contract Drift from Implementation

**Risk**: Spec documents HTTP 429, but implementation returns HTTP 400; swagger-ui doesn't match actual behavior.

**Mitigation**:
- ✅ Task T052: ContractIT validates spec vs implementation
- ✅ Task T052e: response schema validation
- ✅ Every error response HTTP code must have corresponding test (T046, T049, T050)
- ✅ Code review: any new error response requires OpenAPI update + test

**CI/CD Gate**: ContractIT must pass before merge to main

---

### Risk 9: Rate-Limit Lockout DoS (Attacker Locks Legitimate Users)

**Risk**: Attacker intentionally triggers 5 failed attempts on legitimate user email; user is locked out for 5-10 min.

**Mitigation** (Accepted Design Decision):
- ✅ This is a known trade-off: per-email rate-limiting MUST allow attacker to lock users (cannot avoid)
- ✅ Operational mitigations:
  - Monitoring: alert on >10 lockouts/hour (suggests attack)
  - Admin dashboard (future): unlock specific email with verification
  - Communication: document "If locked out, wait 5-10 minutes and retry"
- ✅ Task T050e (FailureHandlingIntegrationTest) includes monitoring strategy
- ℹ️ Long-term: Admin Dashboard feature will add emergency unlock endpoint

**Decision Document**: See research.md "Q1: Brute-Force Attack Defense Strategy" — per-email is least-worst trade-off

---

### Risk 10: Concurrent Request Request Order Dependency

**Risk**: Client sends 2 requests simultaneously: one valid, one invalid. Timing determines if counter increments before success resets it.

**Mitigation**:
- ✅ Race condition is benign: worst case, counter reaches 6 (still HTTP 429)
- ✅ Test T049h: execute concurrent requests; verify result is either locked or success (no in-between state)
- ✅ Use AtomicInteger for all counter operations (guaranteed happens-before semantics)

**Accepted Behavior**: If simultaneous valid + invalid request, system may briefly lock then unlock. Not a security issue, only usability edge case.

---

## Implementation Strategy

### MVP Scope (Recommended Phase 1 Release)

**Focus**: Implement US1 + US2 core authentication (US3 optional but recommended for discoverability).

**Deliverables**:
- Spring Security HTTP Basic configuration (T011-T020)
- EmpleadoUserDetailsService with password validation (T021-T025)
- RateLimitFilter with per-email brute-force defense (T026-T033)
- AuthAuditFilter with secure audit logging (T034-T038)
- Integration tests for success/failure/rate-limit/failure-handling (T044-T051)
- OpenAPI documentation (T039-T043) ✅ RECOMMENDED (low effort, high value)
- Unit tests (T053-T057)
- Code quality gates (T058-T061)

**Estimated Effort**: 50-60 hours  
**Timeline**: 3-4 weeks @ 16 hrs/week  
**Quality Gates**: All 75 tasks completed + code coverage >= 80%

---

### Phased Delivery (Recommended)

#### Delivery 1: Core Auth + Rate-Limiting (Week 1-2)
- Phases: T001-T038, T044-T057
- Status: Feature branch ready for review
- Tests: All integration tests passing

#### Delivery 2: Documentation + Quality (Week 3)
- Phases: T039-T043, T058-T075
- Status: PR ready for merge
- Tests: ContractIT passing; code coverage validated

---

### Resource Allocation

**Recommended Team Structure** (for 4-week timeline):

| Role | Tasks | Effort | Duration |
|------|-------|--------|----------|
| **Lead (Architect)** | T001-T020, T039-T043, T071-T073 | 25 hrs | Full project |
| **Backend Eng 1** | T021-T025, T044-T045, T053, T071 | 20 hrs | Weeks 1-3 |
| **Backend Eng 2** | T026-T038, T049-T051, T054-T057, T071 | 22 hrs | Weeks 1-3 |
| **QA Eng** | T046-T052, T058-T070, T074-T075 | 18 hrs | Weeks 2-4 |
| **DevOps** | T007-T008, T062-T064, T069 | 5 hrs | Weeks 1, 4 |

**Total Effort**: ~90 person-hours (achievable in 4-5 weeks)

---

### Testing Strategy (Recommended Approach)

**Layer 1: Unit Tests** (T053-T057) — Run first, fast feedback
- EmpleadoUserDetailsService unit tests
- RateLimitService unit tests
- SecurityConfig validation tests
- Filter unit tests

**Layer 2: Integration Tests** (T044-T052) — Run after Layer 1
- Authentication success flow (T044-T045)
- Invalid credentials flow (T046)
- Rate-limiting flow (T049)
- Failure handling flow (T050)
- Contract validation (T052)

**Layer 3: E2E Tests** (T066-T070) — Run before merge
- Full lifecycle test: start PostgreSQL → authenticate → rate-limit → recover
- Swagger UI "Authorize" button manual test
- OpenAPI spec validation

**Layer 4: Post-Deploy Monitoring** (T074-T075) — Production validation
- Auth error rate monitoring (alert >1% failures)
- Rate-limit lockout monitoring (alert >10/hour)
- Audit log rotation verification

---

### Code Review Checklist (For Reviewers)

Required sign-off before merge:

- [ ] **Security Lead Review**:
  - [ ] No plaintext passwords in logs (grep audit logs)
  - [ ] Constant-time hash comparison verified (T059b)
  - [ ] Rate-limit cannot be bypassed (T049 tests comprehensive)
  - [ ] Fail-secure: no fallback/caching (T050 tests verify)
  - [ ] Authentication header masked in logs (T035, T057c)

- [ ] **Backend Lead Review**:
  - [ ] Spring Security configuration follows Spring 6.x best practices
  - [ ] EmpleadoUserDetailsService handles all error cases
  - [ ] No session state leakage (T044b test validates)
  - [ ] Code coverage >= 80% (T058 reports)
  - [ ] All tasks completed and tested (T001-T075)

- [ ] **Ops/DevOps Review**:
  - [ ] Audit logging configured for 90-day retention (T035, T061)
  - [ ] Monitoring alerts configured (T064)
  - [ ] Docker Compose includes PostgreSQL setup (T069)
  - [ ] Deployment runbook complete (T064)

---

## Validation Checklist Before Merge

**MUST-HAVE** (Blocking merge):

- [ ] All 75 tasks marked "done" or "in review"
- [ ] All integration tests pass (`mvn verify`)
- [ ] Code coverage >= 80% for auth classes (SonarQube report)
- [ ] Swagger UI renders "Authorize" button and accepts credentials
- [ ] OpenAPI spec validates against 3.0.0 schema
- [ ] No plaintext passwords in logs (manual audit log review)
- [ ] Constant-time hash comparison verified (code review gate T059b)
- [ ] PR has atomic commits with spec/tasks references
- [ ] Security + Backend leads approved PR

**NICE-TO-HAVE** (Non-blocking):

- [ ] E2E test in staging environment passes (T074)
- [ ] Performance test: auth <100ms per SC-001 (T044a jMH benchmark)
- [ ] Load test: 100 concurrent requests per SC-006 (T049h extended)
- [ ] Audit log rotation tested (90-day retention verified)

---

## Success Metrics & Monitoring

### Pre-Launch Metrics (Dev/Test)

| Metric | Target | Validation |
|--------|--------|-----------|
| Code Coverage | >= 80% | SonarQube report |
| Test Pass Rate | 100% | CI/CD build green |
| Security Review | Approved | Lead signature |
| Spec Compliance | 40-60 tasks done | Task checklist 100% |

### Post-Launch Metrics (Production)

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Auth Success Rate | >= 99.5% | < 99.5% |
| Avg Auth Latency | < 100ms | > 150ms |
| Rate-Limit Lockouts | < 10/hour | > 50/hour (attack pattern) |
| Service Uptime | >= 99.9% | < 99.9% |
| Audit Log Retention | 90 days | < 60 days |

---

**Version**: 1.0  
**Last Updated**: 2026-03-17  
**Feature Branch**: `007-basic-auth-signin`  
**Ready for Implementation**: ✅ YES
