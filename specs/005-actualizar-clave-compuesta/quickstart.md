# Quickstart: CRUD Empleados con Clave Compuesta

## 1) Prerrequisitos

- Java 17
- Maven 3.9+
- Docker y Docker Compose

## 2) Levantar PostgreSQL en Docker

1. Crear/ajustar `docker/docker-compose.yml` con servicio PostgreSQL.
2. Iniciar base de datos:

```bash
docker compose -f docker/docker-compose.yml up -d
```

3. Verificar contenedor activo:

```bash
docker ps
```

## 3) Configuración de aplicación

1. Definir conexión PostgreSQL en `src/main/resources/application.yml`.
2. Configurar seguridad Basic Auth en Spring Security.
3. Crear migraciones para:
   - tabla `empleado` con PK compuesta (`prefijo`, `consecutivo`)
   - secuencia de consecutivo (`empleado_consecutivo_seq`) o equivalente

## 4) Ejecutar aplicación

```bash
mvn spring-boot:run
```

## 5) Validar contrato OpenAPI/Swagger

- Contrato planificado: `specs/005-actualizar-clave-compuesta/contracts/empleados.openapi.yaml`
- Swagger UI esperado: `http://localhost:8080/swagger-ui/index.html`

## 6) Pruebas de integración mínimas obligatorias

1. Seguridad
   - `GET /api/empleados` sin credenciales -> `401`
   - `GET /api/empleados` con credenciales válidas -> `200`

2. Clave compuesta autogenerada
   - `POST /api/empleados` válido -> `201`
   - Respuesta incluye `clave` con patrón `EMP-` + dígitos
   - No se acepta clave manual en payload de alta

3. CRUD por clave compuesta
   - `GET /api/empleados/{clave}` existente -> `200`
   - `PUT /api/empleados/{clave}` válido -> `200`
   - `DELETE /api/empleados/{clave}` -> `204`
   - Operaciones sobre clave inexistente -> `404`

4. Concurrencia de alta
   - Ejecución concurrente de altas válidas -> 0 colisiones de clave

5. Validaciones
   - `nombre`/`direccion`/`telefono` vacíos -> `400`
   - `nombre`/`direccion`/`telefono` > 100 -> `400`

## 7) Criterios de salida

- CRUD funcional con clave compuesta autogenerada.
- Endpoints protegidos con Basic Auth.
- Persistencia PostgreSQL en Docker (local/CI).
- Contrato OpenAPI actualizado y Swagger accesible.
- Pruebas de integración de auth, DB y contrato ejecutadas.