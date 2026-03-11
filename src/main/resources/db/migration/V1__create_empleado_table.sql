CREATE TABLE empleado (
    prefijo VARCHAR(4) NOT NULL DEFAULT 'EMP-',
    consecutivo BIGINT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    direccion VARCHAR(100) NOT NULL,
    telefono VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    CONSTRAINT pk_empleado PRIMARY KEY (prefijo, consecutivo),
    CONSTRAINT chk_prefijo_empleado CHECK (prefijo = 'EMP-'),
    CONSTRAINT chk_nombre_no_vacio CHECK (char_length(trim(nombre)) > 0),
    CONSTRAINT chk_direccion_no_vacia CHECK (char_length(trim(direccion)) > 0),
    CONSTRAINT chk_telefono_no_vacio CHECK (char_length(trim(telefono)) > 0)
);
