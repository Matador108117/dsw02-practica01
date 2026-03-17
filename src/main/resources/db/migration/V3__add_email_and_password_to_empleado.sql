ALTER TABLE empleado
    ADD COLUMN IF NOT EXISTS correo_electronico VARCHAR(150),
    ADD COLUMN IF NOT EXISTS contrasena VARCHAR(255);

-- Backfill deterministic placeholders for existing rows so NOT NULL can be enforced safely.
UPDATE empleado
SET
    correo_electronico = COALESCE(
        correo_electronico,
        lower('migrated-' || prefijo || consecutivo || '@local.invalid')
    ),
    contrasena = COALESCE(contrasena, 'migrated-pending-reset')
WHERE correo_electronico IS NULL
   OR contrasena IS NULL;

ALTER TABLE empleado
    ALTER COLUMN correo_electronico SET NOT NULL,
    ALTER COLUMN contrasena SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_empleado_correo_lower
    ON empleado (lower(correo_electronico));
