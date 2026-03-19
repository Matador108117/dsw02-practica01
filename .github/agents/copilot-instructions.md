# dsw02-practica01 Development Guidelines

Auto-generated from all feature plans. Last updated: 2026-02-26

## Active Technologies
- PostgreSQL (Docker-managed in local and CI) (005-actualizar-clave-compuesta)
- Java 17 + Spring Boot 3.4.2, Spring Security (HTTP Basic + PasswordEncoder), Spring Data JPA, springdoc-openapi 2.8.4, Bcrypt/Argon2 for password hashing, Flyway for DB migrations (007-basic-auth-signin)
- PostgreSQL 15 (Docker-managed in local dev and CI/CD environments) (007-basic-auth-signin)
- Java 17 + Spring Boot 3, Spring Security, Spring Data JPA, springdoc-openapi, Angular 22 LTS, Cypress (009-frontend-angular-v1)

- Java 17 + Spring Boot 3, Spring Security, Spring Data JPA, springdoc-openapi (003-crud-empleados)
- Angular 22 LTS + TypeScript, nvm-managed Node runtime, Cypress E2E, Dockerized frontend integrated in existing docker-compose topology (official frontend platform)

## Project Structure

```text
backend/
frontend/
tests/
docker/
```

## Commands

# Add commands for Java 17

# Frontend
- `cd frontend && nvm use && npm ci`
- `cd frontend && npm run test:e2e` (Cypress; build gate)
- `cd docker && docker compose up -d --build` (backend + frontend integration)

## Code Style

Java 17: Follow standard conventions
TypeScript/Angular: use strict typing; frontend must consume only official API
endpoints, delegate authentication to API, and avoid backend business-logic duplication

## Recent Changes
- 009-frontend-angular-v1: Added Java 17 + Spring Boot 3, Spring Security, Spring Data JPA, springdoc-openapi, Angular 22 LTS, Cypress
- 008-api-v3-departamentos: Added Java 17 + Spring Boot 3, Spring Security, Spring Data JPA, springdoc-openapi
- 007-basic-auth-signin: Added Java 17 + Spring Boot 3.4.2, Spring Security (HTTP Basic + PasswordEncoder), Spring Data JPA, springdoc-openapi 2.8.4, Bcrypt/Argon2 for password hashing, Flyway for DB migrations


<!-- MANUAL ADDITIONS START -->
<!-- MANUAL ADDITIONS END -->
