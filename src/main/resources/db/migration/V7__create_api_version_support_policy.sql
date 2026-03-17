-- V7: Create api_version_support_policy table for runtime sunset enforcement
-- Purpose: Store version transition points for v1 deprecation and v2 sunset enforcement

CREATE TABLE IF NOT EXISTS api_version_support_policy (
    id BIGSERIAL PRIMARY KEY,
    api_name VARCHAR(100) NOT NULL UNIQUE,
    deprecated_version VARCHAR(10) NOT NULL,  -- e.g., 'v1'
    active_version VARCHAR(10) NOT NULL,      -- e.g., 'v2'
    deprecation_notice TEXT,
    release_v2_at_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    sunset_at_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for efficient lookups
CREATE UNIQUE INDEX idx_api_version_support_policy_name ON api_version_support_policy(api_name);

-- Validation: sunset_at_utc must be >= release_v2_at_utc
ALTER TABLE api_version_support_policy ADD CONSTRAINT chk_sunset_after_release 
    CHECK (sunset_at_utc >= release_v2_at_utc);

-- Add comment
COMMENT ON TABLE api_version_support_policy IS 'Runtime policy for API version lifecycle: v1 deprecation and v2 sunset enforcement';
COMMENT ON COLUMN api_version_support_policy.release_v2_at_utc IS 'Date v2 was released (immutable, used as reference for 90-day window, UTC only)';
COMMENT ON COLUMN api_version_support_policy.sunset_at_utc IS 'Date v1 definitively sunsets (410 Gone after this point, UTC only)';
