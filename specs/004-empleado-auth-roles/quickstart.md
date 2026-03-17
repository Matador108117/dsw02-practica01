# Quickstart: Gestion de credenciales y roles de empleado

## Prerequisites
- Java 17
- Maven 3.9+
- Docker y Docker Compose

## 1) Levantar base de datos
```bash
cd /home/matador1081/semestre6/deploy/dsw02-practica01
docker compose -f docker/docker-compose.yml up -d
```

## 2) Configurar variables de entorno de aplicacion
```bash
export DB_URL=jdbc:postgresql://localhost:5432/empleados_db
export DB_USER=empleados
export DB_PASSWORD=empleados
export APP_BOOTSTRAP_ADMIN_EMAIL=admin@empresa.com
export APP_BOOTSTRAP_ADMIN_PASSWORD='Admin123!'
```

## 3) Ejecutar aplicacion
```bash
cd /home/matador1081/semestre6/deploy/dsw02-practica01
mvn spring-boot:run
```

## 4) Validar rutas versionadas y deprecacion
- Verificar contrato y UI:
  - `GET /v3/api-docs`
  - `GET /swagger-ui/index.html`
- Verificar coexistencia temporal:
  - `GET /api/v1/empleados` (deprecada)
  - `GET /api/v2/empleados` (activa)
- Verificar encabezados de deprecacion en v1:
  - `Deprecation: true`
  - `Sunset: <fecha-release-v2+90d>`
- Verificar politica temporal oficial:
  - `release_v2_at_utc` inmutable (UTC)
  - `sunset_at_utc = release_v2_at_utc + 90 dias`

## 5) Validar autorizacion por roles
- Validar que endpoints protegidos usen HTTP Basic (`type=http`, `scheme=basic`).
- Validar que el username de Basic Auth sea `correo_electronico`.
- Validar que `contrasena` sea transitoria y se compare por hash contra
  `contrasena_hash` persistido en `empleado`.
- Usuario `ADMIN` debe poder POST, GET, PUT, DELETE.
- Usuario `USER` debe poder solo GET.

Ejemplo de consulta con `USER`:
```bash
curl -i -u user@empresa.com:User123! http://localhost:8080/api/v2/empleados
```

Ejemplo de escritura rechazada con `USER`:
```bash
curl -i -u user@empresa.com:User123! \
  -H 'Content-Type: application/json' \
  -d '{"nombre":"A","direccion":"B","telefono":"C","correoElectronico":"x@y.com","contrasena":"Temp12345"}' \
  http://localhost:8080/api/v2/empleados
```

## 6) Validar bloqueo por intentos fallidos
1. Ejecutar 6 intentos con contrasena invalida para mismo correo+IP dentro de 15 minutos.
2. Confirmar `401` en intentos invalidos previos al bloqueo.
3. Confirmar `423` en el intento excedente durante bloqueo activo.
3. Esperar 15 minutos o ajustar reloj de prueba para confirmar desbloqueo.

## 7) Validar unicidad case-insensitive de correo
1. Crear empleado con correo `Ana@example.com`.
2. Intentar crear empleado con correo `ana@example.com`.
3. Confirmar rechazo por conflicto de unicidad.

## 8) Ejecutar pruebas
```bash
cd /home/matador1081/semestre6/deploy/dsw02-practica01
mvn test
```

## 8.1) Validar corte definitivo de v1
1. Confirmar que antes de `sunset_at_utc` la ruta `GET /api/v1/empleados` responde `200`.
2. Simular/reproducir tiempo posterior a `sunset_at_utc` en entorno de prueba controlado.
3. Confirmar que `GET /api/v1/empleados` responde `410 Gone` sin bypass de configuracion.

## 9) Evidencia minima para PR
- Pruebas de contrato para v1 (deprecada) y v2 (activa).
- Pruebas de integracion de authN/authZ y limite de intentos.
- Pruebas de semantica HTTP (`401` vs `423`).
- Pruebas de sunset con semantica HTTP (`410` post-sunset en v1).
- Pruebas de unicidad de correo case-insensitive.
- Evidencia de migracion de esquema para campos obligatorios y rol.
- Evidencia de rendimiento en CI: P95 auth <= 2000 ms y P95 listados <= 800 ms.
