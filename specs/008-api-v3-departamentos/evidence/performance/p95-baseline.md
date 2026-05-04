# Evidencia de Performance P95

Fecha: 2026-03-18
Entorno: Docker Compose local (`docker/docker-compose.yml`)
API: `http://localhost:8080`
Autenticacion: HTTP Basic (`admin@empresa.com` / `Admin123!`)
Muestra por endpoint: 30 solicitudes HTTP consecutivas

## Resultados

- `GET /api/v3/departamentos?page=0&size=25`
  - P95 observado: `0.202658 s` (202.658 ms)
  - Objetivo: `<= 800 ms`
  - Estado: PASS

- `GET /api/v3/departamentos/{id}/empleados?page=0&size=25`
  - P95 observado: `0.205987 s` (205.987 ms)
  - Objetivo: `<= 1000 ms`
  - Estado: PASS

## Comandos usados

```bash
LIST_P95=$(for i in $(seq 1 30); do \
  curl -s -o /dev/null -w '%{time_total}\n' -u admin@empresa.com:Admin123! \
    'http://localhost:8080/api/v3/departamentos?page=0&size=25'; \
done | sort -n | awk '{a[NR]=$1} END{idx=int((NR*95+99)/100); if(idx<1) idx=1; print a[idx]}')

REL_P95=$(for i in $(seq 1 30); do \
  curl -s -o /dev/null -w '%{time_total}\n' -u admin@empresa.com:Admin123! \
    'http://localhost:8080/api/v3/departamentos/DEP-000002/empleados?page=0&size=25'; \
done | sort -n | awk '{a[NR]=$1} END{idx=int((NR*95+99)/100); if(idx<1) idx=1; print a[idx]}')
```
