-- V8: Backfill default USER role for existing records (idempotent)
-- Purpose: Initialize rol column with 'USER' for all existing empleados without breaking changes

-- Check if rol column exists, add it if not
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'empleado' AND column_name = 'rol'
  ) THEN
    -- Add rol column with default 'USER'
    ALTER TABLE empleado ADD COLUMN rol VARCHAR(20) NOT NULL DEFAULT 'USER';
    
    -- Add constraint to restrict values
    ALTER TABLE empleado ADD CONSTRAINT chk_rol_enum 
      CHECK (rol IN ('ADMIN', 'USER'));
    
    -- Add index for efficient role-based queries
    CREATE INDEX idx_empleado_rol ON empleado(rol);
    
    -- Add comment
    COMMENT ON COLUMN empleado.rol IS 'Role for authorization: ADMIN (full CRUD), USER (read-only)';
  END IF;
END $$;

-- Verify no records are created without a role (idempotent validation)
-- This ensures data integrity post-migration
DO $$
DECLARE
  null_rol_count INT;
BEGIN
  SELECT COUNT(*) INTO null_rol_count FROM empleado WHERE rol IS NULL;
  IF null_rol_count > 0 THEN
    RAISE EXCEPTION 'Data integrity check failed: % records have NULL rol', null_rol_count;
  END IF;
END $$;
