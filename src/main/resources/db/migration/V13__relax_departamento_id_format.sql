-- Relax departamento id format to support legacy IDs (e.g., DEP-1, DEP-TST)
ALTER TABLE departamento
    DROP CONSTRAINT IF EXISTS chk_departamento_id_formato;

ALTER TABLE departamento
    ADD CONSTRAINT chk_departamento_id_formato
    CHECK (id ~ '^DEP-[A-Za-z0-9]{1,6}$');
