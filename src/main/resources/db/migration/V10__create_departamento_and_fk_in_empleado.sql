CREATE TABLE IF NOT EXISTS departamento (
    id VARCHAR(10) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    CONSTRAINT pk_departamento PRIMARY KEY (id),
    CONSTRAINT chk_departamento_id_formato CHECK (id ~ '^DEP-[0-9]{6}$'),
    CONSTRAINT chk_departamento_nombre_no_vacio CHECK (char_length(trim(nombre)) > 0)
);

CREATE SEQUENCE IF NOT EXISTS departamento_consecutivo_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1;

ALTER TABLE empleado
    ADD COLUMN IF NOT EXISTS departamento_id VARCHAR(10);

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.table_constraints
    WHERE table_name = 'empleado'
      AND constraint_name = 'fk_empleado_departamento'
  ) THEN
    ALTER TABLE empleado
      ADD CONSTRAINT fk_empleado_departamento
      FOREIGN KEY (departamento_id)
      REFERENCES departamento(id)
      ON DELETE RESTRICT;
  END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_empleado_departamento_id ON empleado(departamento_id);
