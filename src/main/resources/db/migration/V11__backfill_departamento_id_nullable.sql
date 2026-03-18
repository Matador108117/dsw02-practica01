-- Mantener nullable para registros historicos y limpiar cualquier referencia invalida.
UPDATE empleado e
SET departamento_id = NULL
WHERE departamento_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM departamento d
    WHERE d.id = e.departamento_id
  );
