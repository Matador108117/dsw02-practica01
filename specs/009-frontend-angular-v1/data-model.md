# Data Model - Frontend Angular V1

## Entity: LoginRequest
- Purpose: Payload sent by frontend to authenticate user.
- Fields:
  - `email` (string, required, email format)
  - `password` (string, required, non-empty)
- Validation rules:
  - Both fields required in frontend before API request.
  - Credentials are sent only in JSON request body.

## Entity: LoginResponse
- Purpose: Successful authentication response.
- Fields:
  - `status` (enum: `ACCEPTED`, required)
  - `role` (enum: `USER`, `ADMIN`, required)
- Validation rules:
  - Response must include both fields on success.
  - Access/refresh tokens are delivered via secure cookies, not payload fields.

## Entity: AuthErrorResponse
- Purpose: Structured error for failed login.
- Fields:
  - `code` (string, required)
  - `message` (string, required)
  - `timestamp` (string, ISO-8601, required)
- Validation rules:
  - Returned with `401 Unauthorized` for invalid credentials.

## Entity: RefreshRequest
- Purpose: Trigger token renewal using refresh cookie.
- Fields:
  - Body: none required
  - Header: anti-CSRF token required for protected cookie flow
- Validation rules:
  - Refresh cookie must be present and valid.
  - CSRF token must match expected server-side value.

## Entity: RefreshResponse
- Purpose: Return renewed access token metadata.
- Fields:
  - `status` (enum: `ACCEPTED`, required)
  - `role` (enum: `USER`, `ADMIN`, required)
  - `expiresIn` (integer seconds, required)

## Entity: SessionState (Frontend)
- Purpose: In-memory UI auth state.
- Fields:
  - `isAuthenticated` (boolean)
  - `role` (enum: `USER`, `ADMIN`, nullable before login)
  - `accessToken` (string, session-scoped)
  - `csrfToken` (string, required for sensitive operations)
- State transitions:
  - `ANONYMOUS` -> `AUTHENTICATING` -> `AUTHENTICATED`
  - `AUTHENTICATED` -> `REFRESHING` -> `AUTHENTICATED`
  - `AUTHENTICATED` -> `ANONYMOUS` on logout/revocation/refresh-expired

## Entity: SidebarEntity
- Purpose: Represents selectable domain entity in navigation.
- Fields:
  - `key` (enum/string, required; e.g. `empleados`, `departamentos`)
  - `label` (string, required)
  - `enabled` (boolean, default true)

## Entity: CrudToggleAction
- Purpose: Encapsulates dashboard action cards.
- Fields:
  - `type` (enum: `ADD`, `EDIT`, `DELETE`, `VIEW_DEPT_EMPLOYEES`)
  - `enabled` (boolean)
  - `targetEntity` (enum: `empleados`, `departamentos`)

## Relationships
- `SessionState` governs access to all `SidebarEntity` and `CrudToggleAction` interactions.
- `CrudToggleAction(type=VIEW_DEPT_EMPLOYEES)` depends on selected `departamento.id`.
- Login/refresh contracts bind frontend session transitions to backend auth responses.
