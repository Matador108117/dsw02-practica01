-- V6: Create auth_attempt table for rate-limiting and brute-force protection
-- Purpose: Track failed authentication attempts per correo_electronico and IP

CREATE TABLE IF NOT EXISTS auth_attempt (
    id BIGSERIAL PRIMARY KEY,
    correo_electronico VARCHAR(150) NOT NULL,
    ip_origen VARCHAR(45) NOT NULL,  -- Support IPv4 and IPv6
    failed_count_window INTEGER NOT NULL DEFAULT 0,
    window_start_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    blocked_until TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Composite key: correo_electronico + IP for per-email+IP tracking
    CONSTRAINT uk_auth_attempt_correo_ip UNIQUE (correo_electronico, ip_origen)
);

-- Index for efficient cleanup queries
CREATE INDEX idx_auth_attempt_blocked_until ON auth_attempt(blocked_until);
CREATE INDEX idx_auth_attempt_correo ON auth_attempt(correo_electronico);

-- Add comment
COMMENT ON TABLE auth_attempt IS 'Tracks authentication attempts for rate-limiting: 5 failures per 15 min, then 15-min lockout';
COMMENT ON COLUMN auth_attempt.failed_count_window IS 'Count of failed attempts in current 15-minute window';
COMMENT ON COLUMN auth_attempt.window_start_at IS 'Start timestamp of current 15-minute window (UTC)';
COMMENT ON COLUMN auth_attempt.blocked_until IS 'If set, authentication is blocked until this timestamp (UTC); NULL = not blocked';
