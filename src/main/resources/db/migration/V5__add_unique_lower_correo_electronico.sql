-- V5: Add case-insensitive unique constraint on correo_electronico
-- Purpose: Ensure correo_electronico is unique regardless of case (per RFC 5321)

-- Drop existing plain unique constraint if it exists.
-- V3 already creates a functional unique index on lower(correo_electronico).
-- PostgreSQL cannot convert expression indexes into UNIQUE CONSTRAINT via USING INDEX.
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.constraint_column_usage
    WHERE table_name = 'empleado'
      AND column_name = 'correo_electronico'
      AND (
        constraint_name = 'empleado_correo_electronico_key'
        OR constraint_name = 'uk_empleado_correo_electronico_ci'
      )
  ) THEN
    ALTER TABLE empleado DROP CONSTRAINT IF EXISTS empleado_correo_electronico_key;
    ALTER TABLE empleado DROP CONSTRAINT IF EXISTS uk_empleado_correo_electronico_ci;
  END IF;
END $$;

-- Ensure case-insensitive uniqueness via functional unique index.
-- Keep V3 index name if present; otherwise create canonical V5 index.
CREATE UNIQUE INDEX IF NOT EXISTS ux_empleado_correo_lower
ON empleado (LOWER(correo_electronico));

CREATE UNIQUE INDEX IF NOT EXISTS idx_empleado_correo_electronico_lower
ON empleado (LOWER(correo_electronico));
