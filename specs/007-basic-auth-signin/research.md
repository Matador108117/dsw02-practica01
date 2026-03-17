# Research: HTTP Basic Authentication in Spring Boot

**Completed**: 2026-03-17  
**Phase**: Phase 0 - Clarification & Research  
**Overall Status**: ALL CLARIFICATIONS RESOLVED ✅

---

## 1. Clarifications Resolved

### Q1: Brute-Force Attack Defense Strategy

**Decision**: Per-email rate limiting with exponential backoff

**Rationale**:
- Per-email keying prevents IP-rotation bypass attacks
- 5 failed attempts per minute is strict enough to block brute-force (2 weeks to exhaust 10k passwords at this rate)
- Exponential backoff (5–10 min) increases cost per retry without permanent lockout
- User experience impact minimal (legitimate users rarely exceed 5 failures/min)

**Alternatives Considered**:
- IP-based rate limiting: Rejected (defeated by proxy/VPN, impacts shared networks)
- Account lockout (permanent): Rejected (DoS vector: attacker locks legitimate accounts)
- No rate limiting: Rejected (violates NFR-001 uptime; brute-force consumes auth service)

**Implementation Path**: Custom RateLimitFilter + Redis cache (or in-memory HashMap for MVP)

---

### Q2: System Uptime & Failure Recovery

**Decision**: Strict 99.9% uptime SLA; fail-secure with HTTP 503 on service failure

**Rationale**:
- 99.9% monthly SLA = max 21.6 minutes unplanned downtime/month
- Fail-secure design prevents credential bypass attacks (no fallback cache)
- HTTP 503 immediately returned (<1s) prevents hanging requests
- RTO <15 minutes ensures rapid user return to service

**Alternatives Considered**:
- Relaxed SLA (99%): Rejected (doubles allowed downtime; unacceptable for employee data)
- Credential caching fallback: Rejected (CRITICAL SECURITY ISSUE: stale credentials bypass revocation)
- Internal server retries: Rejected (delays response, looks like hang; 503 is cleaner)

**Implementation Path**: Spring Security exception handler translating AuthenticationException → HTTP 503

---

### Q3: Credential Management Scope

**Decision**: Out-of-scope for this feature; deferred to Admin Dashboard feature

**Rationale**:
- Scope boundary keeps feature focused on authentication enforcement only
- Credential management (reset, unlock, admin override) adds 20+ requirements
- Admin Dashboard feature will consolidate all admin operations
- MVP credentials created via database migration scripts for now

**Alternatives Considered**:
- In-scope password reset endpoint: Rejected (increases feature scope by 30%; adds email dependency)
- In-scope admin unlock: Rejected (requires admin UI; deferred to dashboard)

**Scope Boundary**: This feature = enforce auth only. Credential lifecycle = Admin Dashboard.

**Out-of-Scope Endpoints** (for future Admin Dashboard):
- `POST /api/v2/admin/reset-password` (future)
- `POST /api/v2/admin/unlock-account` (future)
- `POST /api/v2/admin/create-empleado` (future)

---

### Q4: Basic Auth vs OAuth2/OIDC (Permanent Choice)

**Decision**: HTTP Basic Auth PERMANENT choice; OAuth2/OIDC explicitly rejected for this phase

**Rationale**:
- **Simplicity**: RFC 7617 Basic Auth is stateless, no token server, no redirect flows
- **Monolithic Architecture**: Internal-only API (DSW02) doesn't need federation or third-party access
- **Immediate Implementation**: Basic Auth in Spring Security requires 20 lines; OAuth2 requires 100+
- **No Migration Path Needed Today**: If true federation needed in future, add API Gateway layer (doesn't modify this service)

**Long-Term Federation Path** (if needed):
- Keep HTTP Basic auth in monolithic service
- Add API Gateway layer (e.g., Kong, Tyk, AWS API Gateway) EXTERNAL to this service
- API Gateway handles OAuth2/OIDC with Empleados API as confidential backend
- This service never changes; gateway handles token validation → HTTP Basic credential injection

**Alternatives Rejected**:
- OAuth2: Requires token server, token refresh logic, PKCE flows. Too complex for internal monolith.
- OIDC: Requires OpenID provider. Not applicable for employee authentication (internal-only).
- API Keys: No per-user context; inappropriate for authentication (acceptable only for service-to-service).
- JWT Bearer Tokens: Require token server, validation logic, refresh. More complex than HTTP Basic without benefit (we're not federation-capable yet).

**Architectural Rationale Document** (from Spec Section "Design Rationale & Trade-offs"):
> "HTTP Basic Authentication was chosen as the permanent authentication scheme for the Empleados API based on three key principles: (1) **Architectural Simplicity** — the monolithic architecture requires no external federation, token server, or third-party integrations; (2) **RFC Compliance** — HTTP Basic (RFC 7617) is a standard, stateless protocol supported natively by Spring Security and HTTP clients; (3) **Implementation Velocity** — Basic Auth in Spring Security requires minimal configuration (~20 lines), enabling rapid MVP delivery. OAuth2/OIDC were explicitly evaluated and rejected because the monolithic architecture provides no current benefit (no federation, no third-party access, no need for delegated authorization). If future requirements mandate multi-tenant or third-party access, this decision will be revisited by adding an external API Gateway layer that handles OAuth2/OIDC transformation without modifying this service."

---

### Q5: Session/Logout/Concurrency Model

**Decision**: Stateless HTTP Basic semantics; logout is client-side only; no concurrent session limits

**Rationale**:
- HTTP Basic is inherently stateless: each request includes full credentials in Authorization header
- Server has no concept of "sessions" or "login" — each request is independent
- Logout = browser stops sending Authorization header (client-side action)
- No session state means no concurrent-session limits, no token revocation
- Simplest possible design; aligns with HTTP Basic RFC 7617

**Implications**:
- **No Session Table Needed**: No schema migrations for session storage
- **No Logout Endpoint**: Logout is client-side (clear credential UI, stop sending Authorization header)
- **No Token Revocation**: Credentials valid until password changed; can't revoke specific login
- **No Concurrent Session Limits**: Multiple concurrent logins from same Empleado are allowed (different devices, browsers)
- **Rate-Limiting Per Email**: Even concurrent requests from same Empleado share the same fail-attempt counter

**Alternatives Considered**:
- Session-based logout: Rejected (adds session table, session cleanup job, statefulness contradiction)
- Token-based (JWT) logout: Rejected (requires token revocation list; defeats stateless benefit)
- Concurrent session limits: Rejected (violates stateless semantics; user on laptop + phone = 2 sessions)

**Client-Side Implementation** (developer guidance):
```javascript
// Browser: Stop sending credentials
fetch('/api/v2/empleados', {
  // Do NOT send Authorization header after logout
  headers: {
    // Omit "Authorization: Basic ..." here
  }
});

// SPA: Clear stored credentials
sessionStorage.removeItem('empleado_email');
sessionStorage.removeItem('empleado_password');
localStorage.removeItem('auth_token'); // if any
```

---

## 2. Threat Model

### Attack Vectors Considered

| Threat | Attack | Mitigation | Implementation |
|--------|--------|-----------|-----------------|
| **Brute-Force Password Guessing** | Attacker tries 10k passwords/hour | Per-email rate-limit: 5 fails/min → 5–10 min lockout | RateLimitFilter + in-memory cache (timestamp map) |
| **Credential Exposure in Transit** | eavesdrop on unencrypted HTTP | HTTPS mandatory in production (HTTP only for local dev) | Spring Security config + nginx reverse proxy |
| **Credential Exposure in Logs** | Admin reads passwords in log files | Exclude Authorization header from logs; use `[redacted]` | AuditFilter + SLF4J pattern config |
| **Timing Attack on Hash Comparison** | Attacker infers password validity via response time | Constant-time hash comparison | Spring Security PasswordEncoder (bcrypt/Argon2 default) |
| **Dictionary Attack with Rainbow Tables** | Attacker uses pre-computed hash tables | Salt + strong hash function (bcrypt/Argon2) | Flyway migration: password hashing required at signup |
| **Credential Bypass via IP Rotation** | Attacker uses proxies to reset rate-limit | Rate-limit keyed by email, not IP | RateLimitFilter: `failed_attempts.get(email)` |
| **Account Lockout DoS** | Attacker locks legitimate accounts | Exponential backoff instead of permanent lock | RateLimitFilter: lockout expires after 5–10 min |
| **Service Failure Credential Fallback** | Legacy code caches old passwords | Fail-secure: HTTP 503 immediately on auth service failure | ExceptionHandler: any auth error → 503 (no fallback) |
| **SQL Injection in Auth Query** | Attacker modifies auth SQL | Parameterized queries (JPA prevents by default) | EmpleadoRepository using JPQL/CriteriaAPI (no raw SQL) |

---

## 3. Existing Authentication Patterns

### Spring Boot 3.4 + Spring Security 6 Best Practices

**Reviewed Patterns**:
1. **HTTP Basic in Spring Security 6** (from Spring Docs):
   - Configuration: `HttpSecurity.httpBasic()`; realm set in `HttpBasicConfigurer`
   - UserDetailsService: Custom bean providing `UserDetails` for Empleado lookup
   - PasswordEncoder: Injected bean; bcrypt recommended, Argon2 supported

2. **Rate Limiting in Spring Boot** (common patterns):
   - **Redis-backed**: `@RateLimiter` from Resilience4J; distributed across instances
   - **In-memory**: Custom filter + ConcurrentHashMap; sufficient for single-instance MVP
   - **Bucket4J library**: Thread-safe, per-key (email), configurable time windows
   - Selected for MVP: In-memory due to single-instance architecture

3. **Exception Handling for HTTP 401/403/429**:
   - `UnauthenticatedEntryPoint`: Called when credentials missing or invalid
   - `AccessDeniedHandler`: Called when user lacks authorization (e.g., ADMIN-only endpoint)
   - Custom handlers return JSON error responses (not HTML login page)

4. **Audit Logging Pattern**:
   - Spring Security `AuthenticationAuditListener`: Captures all auth attempts
   - Log format: timestamp, email (not password), success/failure, IP, user-agent
   - Retention: 90 days (configurable via logback.xml)

---

## 4. Technical Dependencies & Versions

### Confirmed Stack

| Component | Version | Notes |
|-----------|---------|-------|
| **Java** | 17 | LTS; required for Spring Boot 3.4 |
| **Spring Boot** | 3.4.2 | LTS; http-basic support stable |
| **Spring Security** | 6.3.1 | Bundled with Spring Boot 3.4; OAuth2/OIDC modules available but unused |
| **Spring Data JPA** | 3.3.5 | Bundled; prevents SQL injection |
| **PostgreSQL** | 15 | Docker container; confirmed in docker-compose.yml |
| **Flyway** | 10.4.1 | Database migrations; no new migrations needed for MVP |
| **bcrypt** | Spring Security built-in | Via `BCryptPasswordEncoder`; configurable rounds (10–14 recommended) |
| **Argon2** | Spring Security built-in | Via `Argon2PasswordEncoder`; future-proof alternative to bcrypt |
| **springdoc-openapi** | 2.8.4 | Existing dependency; HTTP Basic security scheme auto-documented |
| **JUnit 5** | Bundled with Spring Boot Test | Integration tests via MockMvc + TestRestTemplate |

### New Dependencies Needed

**None for MVP** — all required libraries already in pom.xml:
- Spring Security: Already present
- bcrypt: Bundled in Spring Security
- Flyway: Already present

### Optional Dependencies (Not Required)

- **Resilience4J** (for distributed rate-limiting): Deferred to future multi-instance scaling
- **Redis** (for distributed cache): Not needed for single-instance MVP
- **Bucket4J**: Excellent library but overkill; custom filter sufficient for MVP

---

## 5. Architectural Decisions Summary

| Decision | Status | Rationale |
|----------|--------|-----------|
| HTTP Basic Auth (permanent) | ✅ DECIDED | RFC 7617 compliance; internal monolith needs no federation |
| Stateless per-request auth | ✅ DECIDED | No session table; logout is client-side; no concurrent session limits |
| Per-email rate-limiting (5/min, 5–10 min backoff) | ✅ DECIDED | Prevents brute-force; per-email (not IP) prevents bypass via proxy rotation |
| Fail-secure on service failure (HTTP 503) | ✅ DECIDED | No credential caching fallback; immediate rejection |
| Custom in-memory RateLimitFilter (MVP) | ✅ DECIDED | Single-instance; sufficient for P1 product; Redis deferred to scaling phase |
| HTTPS mandatory (production) | ✅ DECIDED | Prevents credential interception; HTTP allowed for local dev only |
| bcrypt/Argon2 password hashing | ✅ DECIDED | Industry standard; constant-time comparison prevents timing attacks |
| Audit logging (90-day retention) | ✅ DECIDED | Supports security investigations; enables SLA monitoring |

---

## 6. Reference Implementations

### Spring Security 6 HTTP Basic Example (from Spring Docs)

```java
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/v2/empleados/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/v2/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> basic
                .realmName("empleados-api")
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(EmpleadoRepository repo) {
        return email -> {
            Empleado emp = repo.findByCorreoElectronico(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));
            return User.builder()
                .username(emp.getCorreoElectronico())
                .password(emp.getContraseñaHash())
                .roles(emp.getRol().name())
                .build();
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // 10–14 rounds
    }
}
```

---

## 7. Next Steps (Phase 1)

- ✅ Clarifications complete
- ⏳ **Generate data-model.md** (entity relationships, rate-limit state machine)
- ⏳ **Generate contracts/empleados-auth-v2.openapi.yaml** (HTTP Basic scheme, endpoint examples)
- ⏳ **Generate quickstart.md** (local dev setup, curl examples, Swagger UI walkthrough)
- ⏳ **Update agent context** (`.github/copilot-instructions.md` with HTTP Basic requirements)

**Estimated Duration**: 2–4 hours (research → contracts → quickstart complete)
