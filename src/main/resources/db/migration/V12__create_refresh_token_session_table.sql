CREATE TABLE IF NOT EXISTS refresh_token_session (
  id BIGSERIAL PRIMARY KEY,
  empleado_clave VARCHAR(20) NOT NULL,
  refresh_token_hash VARCHAR(255) NOT NULL,
  csrf_token VARCHAR(255) NOT NULL,
  expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
  revoked BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_refresh_session_token_hash
  ON refresh_token_session(refresh_token_hash);

CREATE INDEX IF NOT EXISTS idx_refresh_session_empleado
  ON refresh_token_session(empleado_clave);
