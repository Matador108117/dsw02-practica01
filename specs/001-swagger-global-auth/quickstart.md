# Quickstart: Correccion de acceso global en Swagger UI

## Prerequisites
- Java 17
- Maven 3.9+
- Docker y Docker Compose

## 1) Levantar base de datos y aplicacion
```bash
cd /home/matador1081/semestre6/deploy/dsw02-practica01
docker compose -f docker/docker-compose.yml up -d
mvn spring-boot:run
```

## 2) Verificar documentacion API y bloqueo inicial
1. Abrir `http://localhost:8080/swagger-ui/index.html`.
2. Confirmar que los endpoints protegidos aparecen con candado y no ejecutan sin auth.
3. Verificar que `/v3/api-docs` es accesible para lectura de contrato.

## 3) Autenticar una sola vez desde Authorize
1. Pulsar Authorize.
2. Ingresar usuario `emailprueba@gmail.com` y contrasena `contra123`.
3. Confirmar ejecucion de varios endpoints protegidos sin nueva ventana de login por metodo.

## 4) Validar invalidacion por 401
1. Forzar credencial invalida/expirada y ejecutar endpoint protegido.
2. Confirmar respuesta `401`.
3. Confirmar que Swagger exige nuevo Authorize para continuar operaciones protegidas.

## 5) Validar bootstrap idempotente de usuario de prueba
1. Arrancar aplicacion con base vacia y verificar creacion del usuario obligatorio.
2. Reiniciar aplicacion y confirmar que no se duplican registros.
3. Si existia usuario con datos incompletos, confirmar normalizacion a estado requerido.
4. Confirmar que la contrasena se mantiene como `contrasena_hash` (sin texto plano).

## 6) Ejecutar pruebas
```bash
cd /home/matador1081/semestre6/deploy/dsw02-practica01
mvn test
```

## 7) Criterio de versionado
- Confirmar que no se crean rutas nuevas `/api/v3/*`.
- Confirmar que la implementacion se mantiene en `v2` por tratarse de cambio interno.
