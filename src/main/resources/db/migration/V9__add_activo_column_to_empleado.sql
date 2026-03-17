-- V9: Add activo flag required by JPA model
-- Purpose: Keep compatibility with existing Empleado entity and auth flows

ALTER TABLE empleado
    ADD COLUMN IF NOT EXISTS activo BOOLEAN;

UPDATE empleado
SET activo = TRUE
WHERE activo IS NULL;

ALTER TABLE empleado
    ALTER COLUMN activo SET NOT NULL,
    ALTER COLUMN activo SET DEFAULT TRUE;
