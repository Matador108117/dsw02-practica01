# Research: CRUD Empleados con Clave Compuesta

## Decision 1: Clave compuesta lógica con prefijo fijo y consecutivo
- Decision: Modelar la identidad de empleado como composición de `prefijo='EMP-'` + `consecutivo` autogenerado, exponiendo `clave` visible en formato `EMP-000001`.
- Rationale: Cumple el requerimiento de prefijo más autonumérico y mantiene legibilidad funcional para usuarios.
- Alternatives considered:
  - Clave UUID: descartada por no cumplir formato solicitado.
  - Clave manual capturada por usuario: descartada por riesgo de colisiones y porque contradice la generación automática.

## Decision 2: Generación de consecutivo en capa transaccional
- Decision: Generar `consecutivo` con estrategia transaccional en base de datos para evitar colisiones en altas concurrentes.
- Rationale: El éxito requiere unicidad total incluso bajo concurrencia (SC-002).
- Alternatives considered:
  - Cálculo `MAX(consecutivo)+1` en aplicación: descartado por condición de carrera.
  - Generación en memoria: descartada por no ser segura en despliegue multi-instancia.

## Decision 3: Persistencia PostgreSQL con constraints de unicidad
- Decision: Persistir `prefijo` y `consecutivo` como columnas separadas con PK compuesta y constraint de unicidad para clave visible.
- Rationale: Refuerza consistencia del modelo y trazabilidad del componente numérico.
- Alternatives considered:
  - Guardar solo `clave` string: descartado por menor control sobre el consecutivo.
  - PK surrogate adicional: descartado por complejidad innecesaria para este alcance.

## Decision 4: Contrato API con clave visible string
- Decision: Exponer en API la clave compuesta como string (`EMP-000001`) y no aceptar clave en payload de creación.
- Rationale: Simplifica consumo del API y evita manipulación manual de identidad.
- Alternatives considered:
  - Exponer prefijo y consecutivo separados al cliente: descartado por mayor complejidad de consumo.

## Decision 5: Validación dual para campos de negocio
- Decision: Mantener validaciones en API y base de datos para `nombre`, `dirección` y `teléfono` (obligatorios, no vacíos, max 100).
- Rationale: Evita inconsistencias entre capa HTTP y persistencia.
- Alternatives considered:
  - Solo validación en DB: descartada por mala experiencia de error para cliente.

## Decision 6: Seguridad y pruebas constitucionales
- Decision: Aplicar HTTP Basic Auth a todo el CRUD y pruebas de integración para auth, DB y contrato.
- Rationale: Requisitos constitucionales II y V, además de BC-006.
- Alternatives considered:
  - Seguridad parcial solo en escritura: descartada por incumplimiento de alcance CRUD protegido.