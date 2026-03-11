# Quickstart: CRUD de Empleados

## 1) Prerrequisitos

- Java 17
- Docker y Docker Compose
- Maven 3.9+

## 2) Levantar PostgreSQL en Docker

1. Crear/ajustar `docker/docker-compose.yml` con un servicio PostgreSQL.
2. Iniciar base de datos:

```bash
docker compose -f docker/docker-compose.yml up -d
```

3. Verificar disponibilidad:

```bash
docker ps
```

## 3) Configurar aplicación

1. Definir conexión PostgreSQL en `src/main/resources/application.yml`.
2. Configurar usuario/contraseña de Basic Auth en Spring Security.
3. Configurar migraciones (Flyway o equivalente) para tabla `empleado`.

## 4) Ejecutar aplicación

```bash
./mvnw spring-boot:run
```

## 5) Verificar contrato API (OpenAPI/Swagger)

- Contrato planificado: `specs/001-crud-empleados/contracts/empleados.openapi.yaml`
- Swagger UI esperado en ejecución: `http://localhost:8080/swagger-ui/index.html`

## 6) Pruebas mínimas obligatorias

1. Integración de seguridad:
   - `GET /api/empleados` sin credenciales -> `401`
   - `GET /api/empleados` con credenciales válidas -> `200`

2. Integración CRUD + persistencia:
   - `POST` empleado válido -> `201`
   - `GET /api/empleados/{clave}` existente -> `200`
   - `PUT /api/empleados/{clave}` válido -> `200`
   - `DELETE /api/empleados/{clave}` -> `204`
   - `GET /api/empleados/{clave}` tras eliminar -> `404`

3. Integración de validación:
   - Campos `nombre`/`direccion`/`telefono` > 100 -> `400`
   - Campos vacíos -> `400`
   - `clave` duplicada en alta -> `409`

## 7) Criterios de salida de la implementación

- Endpoints CRUD protegidos con Basic Auth.
- Persistencia PostgreSQL funcionando en Docker (local/CI).
- Swagger/OpenAPI actualizado y accesible.
- Cobertura de pruebas de integración de auth, DB y contrato.