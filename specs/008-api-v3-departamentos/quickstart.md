# Quickstart: API v3 Departamentos y Relacion Empleados

## Prerequisites
- Java 17
- Maven 3.9+
- Docker y Docker Compose

## 1) Levantar base de datos
```bash
cd /home/matador1081/semestre6/deploy/dsw02-practica01
docker compose -f docker/docker-compose.yml up -d
```

## 2) Ejecutar aplicacion
```bash
cd /home/matador1081/semestre6/deploy/dsw02-practica01
mvn spring-boot:run
```

## 3) Verificar endpoints de contrato v3
- Swagger UI: `GET /swagger-ui/index.html`
- OpenAPI: `GET /v3/api-docs`

## 4) Probar CRUD de departamentos (v3)
Crear departamento:
```bash
curl -i -u admin@empresa.com:Admin123! \
  -H 'Content-Type: application/json' \
  -d '{"nombre":"Finanzas"}' \
  http://localhost:8080/api/v3/departamentos
```

Listar departamentos (paginado por defecto `page=0&size=25`):
```bash
curl -i -u admin@empresa.com:Admin123! \
  'http://localhost:8080/api/v3/departamentos?page=0&size=25'
```

Consultar por ID:
```bash
curl -i -u admin@empresa.com:Admin123! \
  http://localhost:8080/api/v3/departamentos/DEP-000001
```

Actualizar departamento:
```bash
curl -i -u admin@empresa.com:Admin123! \
  -X PUT -H 'Content-Type: application/json' \
  -d '{"nombre":"Finanzas Corporativas"}' \
  http://localhost:8080/api/v3/departamentos/DEP-000001
```

Eliminar departamento sin empleados asociados:
```bash
curl -i -u admin@empresa.com:Admin123! \
  -X DELETE http://localhost:8080/api/v3/departamentos/DEP-000001
```

## 5) Probar endpoint relacional obligatorio
```bash
curl -i -u admin@empresa.com:Admin123! \
  'http://localhost:8080/api/v3/departamentos/DEP-000001/empleados?page=0&size=25'
```

## 6) Probar POST/PUT de empleados con `departamento_id`
Crear empleado con departamento:
```bash
curl -i -u admin@empresa.com:Admin123! \
  -H 'Content-Type: application/json' \
  -d '{"nombre":"Ana","direccion":"Calle 1","telefono":"555-111","correoElectronico":"ana@empresa.com","contrasena":"Temp12345!","departamentoId":"DEP-000001"}' \
  http://localhost:8080/api/v3/empleados
```

Crear empleado sin departamento (`departamentoId` omitido):
```bash
curl -i -u admin@empresa.com:Admin123! \
  -H 'Content-Type: application/json' \
  -d '{"nombre":"Luis","direccion":"Calle 2","telefono":"555-222","correoElectronico":"luis@empresa.com","contrasena":"Temp12345!"}' \
  http://localhost:8080/api/v3/empleados
```

Actualizar empleado y reasignar departamento:
```bash
curl -i -u admin@empresa.com:Admin123! \
  -X PUT -H 'Content-Type: application/json' \
  -d '{"departamentoId":"DEP-000002"}' \
  http://localhost:8080/api/v3/empleados/EMP-000001
```

## 7) Validar errores esperados
- `422 Unprocessable Entity` cuando `departamentoId` no exista en POST/PUT de empleados.
- `409 Conflict` cuando se intente eliminar un departamento con empleados asociados.
- `401`/`403` segun autenticacion/autorizacion de rol.

## 8) Ejecutar pruebas
```bash
cd /home/matador1081/semestre6/deploy/dsw02-practica01
mvn test
```

## 9) Evidencia minima para PR
- Pruebas de contrato para endpoints nuevos/ajustados en `/api/v3`.
- Pruebas de integracion para FK y validacion de `departamento_id`.
- Prueba negativa de `DELETE` con dependencias (`409`).
- Prueba negativa de referencia inexistente en empleados (`422`).
- Evidencia de paginacion (`page`, `size`, max `100`).

## 10) Verificar sunset v1 (`410 Gone`)

Validar por prueba de integracion con cutoff forzado a pasado:

```bash
cd /home/matador1081/semestre6/deploy/dsw02-practica01
mvn -Dtest=ApiVersionSunsetIntegrationIT test
```

## 11) Ejecutar set focal de regresion

```bash
cd /home/matador1081/semestre6/deploy/dsw02-practica01
mvn -Dtest=ApiVersionSupportPolicyServiceTest,FlywayDepartamentosMigrationIT,DepartamentoCrudContractIT,DepartamentoEmpleadosContractIT test
```

## 12) Revisar evidencia de performance

- Ver `specs/008-api-v3-departamentos/evidence/performance/p95-baseline.md`.
