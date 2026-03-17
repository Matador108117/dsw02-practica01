-- V5: Add case-insensitive unique constraint on correo_electronico
-- Purpose: Ensure correo_electronico is unique regardless of case (per RFC 5321)

-- Drop existing unique constraint if it exists (to replace with case-insensitive version)
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.constraint_column_usage
    WHERE table_name = 'empleado' AND column_name = 'correo_electronico'
    AND constraint_name LIKE '%correo%unique%' OR constraint_name LIKE '%unique%correo%'
  ) THEN
    ALTER TABLE empleado DROP CONSTRAINT IF EXISTS empleado_correo_electronico_key;
  END IF;
END $$;

-- Create case-insensitive unique index
CREATE UNIQUE INDEX idx_empleado_correo_electronico_lower 
ON empleado(LOWER(correo_electronico));

-- Add constraint using the index
ALTER TABLE empleado ADD CONSTRAINT uk_empleado_correo_electronico_ci UNIQUE USING INDEX idx_empleado_correo_electronico_lower;
