-- V4: Migrate contrasena legacy -> contrasena_hash (hash-only credential model)
-- Purpose: Enable hash-only password storage without persisting plaintext
-- Strategy: Rename existing contrasena to contrasena_hash for backward compatibility

-- Check if contrasena column exists (from V3) and rename to contrasena_hash
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'empleado' AND column_name = 'contrasena'
  ) THEN
    -- Column exists, rename it to contrasena_hash
    ALTER TABLE empleado RENAME COLUMN contrasena TO contrasena_hash;
    
    -- Add comment explaining it's hash-only
    COMMENT ON COLUMN empleado.contrasena_hash IS 'bcrypt hash of password (hash-only, plaintext never persisted)';
  END IF;
END $$;
