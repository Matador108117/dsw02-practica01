# Data Model: HTTP Basic Authentication

**Phase**: Phase 1 - Design  
**Status**: Design Complete  
**Last Updated**: 2026-03-17

---

## 1. Entity Relationships

### Core Entity: `Empleado` (Existing)

```yaml
Entity: Empleado
Primary Key: id_empleado (BIGINT, auto-sequence)
Unique Constraints:
  - correo_electronico (email, case-insensitive)

Attributes:
  - id_empleado           : BIGINT         [PK, auto-generated]
  - nombre                : VARCHAR(100)   [NOT NULL]
  - apellido              : VARCHAR(100)   [NOT NULL]
  - correo_electronico    : VARCHAR(255)   [NOT NULL, UNIQUE]
  - contrasena_hash       : VARCHAR(255)   [NOT NULL, bcrypt/Argon2 hash]
  - rol                   : ENUM           [NOT NULL, values: USER, ADMIN]
  - activo                : BOOLEAN        [NOT NULL, default: true]
  - fecha_creacion        : TIMESTAMP      [NOT NULL, auto-set]
  - fecha_ultima_activ    : TIMESTAMP      [nullable, updated on login success]
  - intentos_fallidos     : INTEGER        [NOT NULL, default: 0, updated by rate-limit filter]
  - bloqueado_hasta       : TIMESTAMP      [nullable, set by rate-limit filter on lockout]

Validation Rules:
  - correo_electronico: Must match RFC 5321 email format
  - contrasena_hash: Must start with "$2a$", "$2b$", "$argon2" (hashlib identifier)
  - rol: Must be 'USER' (read-only empleados) or 'ADMIN' (full CRUD)
  - activo: Only active empleados can authenticate
```

### New Runtime Entity: `RateLimitState` (In-Memory Only, NOT persisted)

```yaml
Entity: RateLimitState
Scope: In-memory HashMap during application runtime
Purpose: Track failed authentication attempts per email

Attributes:
  - email                 : STRING          [key]
  - failed_count          : INTEGER         [attempts in current window]
  - window_start_time     : TIMESTAMP       [when current 1-minute window started]
  - lockout_until         : TIMESTAMP       [when rate limit expires, if locked]

State Transitions:
  1. "unlocked" (count=0): Can accept auth attempts
  2. "rate_limited" (count≥5, now < lockout_until): Reject with HTTP 429
  3. "cooldown_expired" (lockout_until < now): Reset to unlocked; count=0

Logic:
  - On each failed auth attempt:
    * Increment failed_count
    * If count == 5: Set lockout_until = now + exponential_backoff(attempt_number)
    * Sample backoff: 5 min (1st lockout), 10 min (2nd), 15 min (3rd), …
  - On either success or cool-down expiry: Reset count=0, window_start_time=now

Implementation: `ConcurrentHashMap<String, RateLimitEntry>` in `RateLimitService`
Cleanup: Background thread removes expired entries every 5 minutes
```

### Authentication Flow Data

```yaml
HTTP Request Flow:
1. Client encodes credentials: "email:password" → Base64
   Example: "juan@dsw.com:secretPass123" → "anVhbkBkc3cuY29tOnNlY3JldFBhc3MxMjM="

2. Client sends Authorization header:
   GET /api/v2/empleados HTTP/1.1
   Authorization: Basic anVhbkBkc3cuY29tOnNlY3JldFBhc3MxMjM=

3. Spring Security processes:
   a. BasicAuthenticationFilter extracts Base64 string
   b. Decodes to "juan@dsw.com:secretPass123"
   c. Splits on ":" → username="juan@dsw.com", password="secretPass123"
   d. EmpleadoUserDetailsService loads Empleado from DB by email
   e. PasswordEncoder.matches(plaintext, hash) validates password (constant-time)
   f. If match: Create Authentication principal; set SecurityContext
   g. If mismatch: Throw AuthenticationException → HTTP 401

4. Server response includes WWW-Authenticate header:
   HTTP/1.1 401 Unauthorized
   WWW-Authenticate: Basic realm="empleados-api"
   Content-Type: application/json
   {
     "error": "Unauthorized",
     "message": "Invalid credentials"
   }
```

---

## 2. Authentication State Machine

```
┌─────────────────────────────────────────────────────────────────┐
│                  EMPLEADO LOGIN STATE MACHINE                   │
└─────────────────────────────────────────────────────────────────┘

State: UNLOCKED
├─ Description: Email has 0 failed attempts or lockout expired
├─ Entry Conditions: 
│  ├─ First login attempt (no prior history)
│  └─ Lockout cool-down expired
├─ Transitions:
│  ├─ [Valid Credentials] → AUTHENTICATED
│  │  └─ Action: Load user role; create Authentication principal; set SecurityContext
│  │  └─ Side-effect: Update fecha_ultima_activ; reset failed_count=0
│  │
│  └─ [Invalid Credentials] → RATE_LIMIT_CHECK
│     └─ Action: Increment failed_count
│     └─ Next: If count<5 → stay UNLOCKED; if count==5 → LOCKED
│

State: LOCKED
├─ Description: Email has 5+ failed attempts within rate-limit window
├─ Duration: 5–10 min exponential backoff (configured per attempt)
├─ Entry Conditions:
│  └─ failed_count reaches 5 within 1-minute window
├─ Transitions:
│  ├─ [Any Auth Attempt] → HTTP 429 (Too Many Requests)
│  │  └─ Reject immediately; DO NOT check credentials (faster)
│  │  └─ Response: "Rate limit exceeded; try again in 5 minutes"
│  │
│  └─ [Lockout Expiry] → UNLOCKED
│     └─ Action: Reset failed_count=0
│

State: AUTHENTICATED
├─ Description: User has successfully provided valid credentials
├─ Entry Conditions:
│  └─ PasswordEncoder.matches(plaintext, stored_hash) == true
├─ Transitions:
│  └─ [Request Complete] → Exit (stateless; no session state)
│     └─ Side-effect: Set SecurityContext for current request
│     └─ No session created; each next request must re-authenticate
│

┌─────────────────────────────────────────────────────────────────┐
│                      RATE LIMIT WINDOW                          │
└─────────────────────────────────────────────────────────────────┘

Timeline:
  T+0s:     Client attempt 1 (invalid) → count=1, window_start=T+0
  T+15s:    Client attempt 2 (invalid) → count=2
  T+30s:    Client attempt 3 (invalid) → count=3
  T+45s:    Client attempt 4 (invalid) → count=4
  T+60s:    Client attempt 5 (invalid) → count=5; locked until T+60s+5min=T+5:00
  T+2:00:   Client attempt 6 (during lockout)  → HTTP 429 (no password check)
  T+5:00:   Lockout expires → count reset to 0; window_start=T+5:00
  T+5:15:   Client attempt 7 (valid)   → Authentication succeeds; count=0

Key Property: Rate-limit window per EMAIL, not per IP
  ├─ Benefit: Cannot bypass via proxy rotation or IP spoofing
  └─ Trade-off: Shared device (family WiFi) affects all users on that email
```

---

## 3. Database Schema Changes

### Summary

**Status**: NO NEW MIGRATIONS REQUIRED for MVP

**Rationale**:
- Empleado table already has `correo_electronico` (unique, email)
- Empleado table already has `contrasena_hash` (bcrypt/Argon2 field)
- Empleado table already has `rol` (ENUM: USER, ADMIN)
- Existing migration V3 adds email + password fields ✅

### Existing Migrations (No Changes Needed)

| Migration | Description | Status |
|-----------|-------------|--------|
| V1__create_empleado_table.sql | Create base Empleado table | ✅ Existing |
| V2__create_empleado_sequence.sql | Auto-increment sequence | ✅ Existing |
| V3__add_email_and_password_to_empleado.sql | Add correo_electronico + contrasena_hash | ✅ Existing |

### Why No New Migrations

1. **Email Column**: Already exists (V3); used as unique identifier for HTTP Basic auth
2. **Password Hash Column**: Already exists (V3); stores bcrypt/Argon2 digest
3. **Role Column**: Already exists (base table); determines authorization (USER vs ADMIN)
4. **Active Status**: Already exists as `activo` boolean; gates authentication
5. **Rate Limiting**: Handled in-memory by `RateLimitService`; no DB persistence needed for MVP

### Optional: Production Rate-Limit Persistence (Future)

If scaling to multi-instance deployment, add migration:

```sql
-- V4__create_rate_limit_tracking.sql (NOT NEEDED FOR MVP)
CREATE TABLE rate_limit_tracking (
  email VARCHAR(255) PRIMARY KEY,
  failed_count INTEGER NOT NULL DEFAULT 0,
  window_start_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  lockout_until TIMESTAMP,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_lockout_until ON rate_limit_tracking(lockout_until);
```

**When Needed**: When scaling to 3+ instances (rate-limit state must be shared across instances)
**When Not Needed**: Current single-instance MVP (in-memory sufficient)

---

## 4. Authentication Flow Diagrams

### Sequence Diagram: Successful Authentication

```
┌────────────┐         ┌──────────────────┐     ┌──────────────┐     ┌─────────────┐
│   Client   │         │  Spring Security │     │  Empleado    │     │  Database   │
└────────────┘         │   Filters        │     │  Service     │     └─────────────┘
      │                └──────────────────┘     └──────────────┘     └─────────────┘
      │
      │ 1. GET /api/v2/empleados
      │    Authorization: Basic [Base64(email:password)]
      ├─────────────────────────────────────────────────────────────>
      │
      │ 2. BasicAuthenticationFilter decodes Base64
      │    Extracts email="juan@dsw.com", password="secretPass123"
      │                          ├─────────────────────┐
      │                          │ (Decode Base64)     │
      │                          └─────────────────────┘
      │
      │ 3. EmpleadoUserDetailsService.loadUserByUsername(email)
      │                          │<─────────────────────┤
      │                          │ SELECT * FROM empleado WHERE correo_electronico=?
      │                          │                      │<──────────────────────┤
      │                          │                      │                       │
      │                          │ Empleado(id, nombre, role, contrasena_hash)
      │                          │                      │──────────────────────>│
      │                          │<─────────────────────┤
      │
      │ 4. PasswordEncoder.matches(plaintext, stored_hash)
      │    ✓ bcrypt("secretPass123") matches stored_hash
      │                          ├──────────────────────┐
      │                          │ (Constant-time      │
      │                          │  comparison)        │
      │                          └──────────────────────┘
      │
      │ 5. Create Authentication + SecurityContext
      │    Principal = UserDetails(email, role)
      │
      │ 6. HTTP 200 OK + Response Body
      │ [{"id":1, "nombre":"Juan", ...}]
      │<─────────────────────────────────────────────────────────────
      │
```

### Sequence Diagram: Rate-Limited Authentication

```
┌────────────┐         ┌──────────────────┐     ┌──────────────┐
│   Client   │         │  Spring Security │     │ RateLimitSvc │
└────────────┘         │   Filters        │     └──────────────┘
      │                └──────────────────┘     └──────────────┘
      │
      │ 1–4. [Same as successful, but credentials INVALID]
      │      PasswordEncoder.matches() = false
      │
      │ 5. RateLimitFilter.onFailedAttempt(email)
      │                          ├─────────────────────┤
      │                          │ failed_count++      │
      │                          │ if count==5:        │
      │                          │   lockout_until =   │
      │                          │   now + 5 min       │
      │                          └─────────────────────┘
      │
      │ 6. If count < 5:
      │    HTTP 401 Unauthorized
      │    WWW-Authenticate: Basic realm="empleados-api"
      │<─────────────────────────────────────────────────────
      │
      │ [Client retries 5th time]
      │
      │ 7. GET /api/v2/empleados
      │    Authorization: Basic [...]
      ├─────────────────────────────────────────────────────────────>
      │
      │ 8. RateLimitFilter.checkRateLimit(email)
      │                          ├─────────────────────┤
      │                          │ if count==5 AND     │
      │                          │ now < lockout_until:│
      │                          │   return HTTP 429   │
      │                          └─────────────────────┘
      │
      │ 9. HTTP 429 Too Many Requests
      │    Retry-After: 300
      │    {"error":"rate_limit_exceeded","retry_after_seconds":300}
      │<─────────────────────────────────────────────────────────
      │
```

### State Diagram: Email Authentication Lifecycle

```
START
  │
  v
┌──────────────────────┐
│ No Prior Attempts    │ ← New email first login
│ count=0              │
└──────┬───────────────┘
       │
       ├─ VALID → AUTHENTICATED ──→ [Reset count] ──→ END
       │
       └─ INVALID ──→ [count++] ──→ LOOP_CHECK
                         │
                         v
                  ┌──────────────────┐
                  │ count < 5?       │
                  └──────┬───────────┘
                         │
           ┌─────────────┴─────────────┐
           │ YES                       │ NO
           v                           v
        LOOP_CHECK            ┌──────────────────────┐
        Return 401            │ LOCKED               │
        count=1,2,3,4         │ lockout_until set    │
           │                  │ count=5              │
           │                  └──────┬───────────────┘
           │                         │
           │      ┌──────────────────┴──────────────────┐
           │      │                                     │
           v      v                                     v
        RETRY  RETRY  …                          LOCK EXPIRES
        (client (client                              │
         waits)  waits)                              │
           │      │                                   │
           └──────┴───────────────────┬───────────────┘
                                      │
                              ┌───────v────────┐
                              │ Reset count=0  │
                              │ Return 429 now │
                              └────────────────┘
```

---

## 5. Entity Relationships Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                      DATABASE SCHEMA                            │
└─────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────┐
│                    EMPLEADO                      │
├──────────────────────────────────────────────────┤
│ id_empleado (PK)               : BIGINT          │
│ nombre                         : VARCHAR(100)    │
│ apellido                       : VARCHAR(100)    │
│ correo_electronico (UNIQUE)    : VARCHAR(255)    │◄─── USED FOR BASIC AUTH
│ contrasena_hash                : VARCHAR(255)    │◄─── BCRYPT/ARGON2 HASH
│ rol                            : ENUM            │◄─── USER | ADMIN
│ activo                         : BOOLEAN         │◄─── GATES AUTH
│ fecha_creacion                 : TIMESTAMP       │
│ fecha_ultima_activ             : TIMESTAMP       │◄─── UPDATED ON LOGIN
│ intentos_fallidos              : INTEGER         │◄─── RATE LIMIT COUNTER
│ bloqueado_hasta                : TIMESTAMP       │◄─── RATE LIMIT EXPIRY
└──────────────────────────────────────────────────┘


┌──────────────────────────────────────────────────┐
│         IN-MEMORY RATE_LIMIT_STATE (MVP)         │
├──────────────────────────────────────────────────┤
│ email (key)                    : VARCHAR(255)    │
│ failed_count                   : INTEGER         │
│ window_start_time              : TIMESTAMP       │
│ lockout_until                  : TIMESTAMP       │
│                                                  │
│ ★ NOT PERSISTED TO DATABASE                    │
│ ★ Lost on application restart (acceptable MVP)  │
│ ★ Future: Persist to DB for distributed cache   │
└──────────────────────────────────────────────────┘

Relationship: EMPLEADO 1 ──→ ∞ RateLimitState
  - One Empleado can have one RateLimitState entry
  - Entry created on first failed login attempt
  - Entry deleted when lockout expires or app restarts
```

---

## 6. Summary: No Schema Migrations Needed

| Feature Requirement | Implementation | Database Impact |
|-------------------|----------------|-----------------|
| Username (email) | Use `correo_electronico` (existing) | ✅ No change |
| Password Hash | Use `contrasena_hash` (existing) | ✅ No change |
| Authentication Enforcement | Spring Security HTTP Basic | ✅ No change |
| Authorization (USER vs ADMIN) | Check `rol` column (existing) | ✅ No change |
| Rate Limiting (5/min per email) | In-memory HashMap | ✅ No change |
| Fail-Secure (HTTP 503) | ExceptionHandler | ✅ No change |
| Audit Logging | Spring Security listeners | ✅ No change |
| Active/Inactive Status | Check `activo` column (existing) | ✅ No change |

**Conclusion**: HTTP Basic Auth implementation is a code-only change. Empleado schema already has all required fields. No migrations needed for MVP.

