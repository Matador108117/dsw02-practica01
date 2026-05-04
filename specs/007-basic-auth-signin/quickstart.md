# QuickStart: HTTP Basic Authentication in Empleados API

**Objective**: Authenticate with the Empleados API using HTTP Basic credentials and explore POST/GET/PUT/DELETE operations.

**Duration**: 15–20 minutes  
**Prerequisite**: Docker, cURL or Postman installed

---

## 1. Start the API with Docker

### Step 1.1: Start PostgreSQL and API

```bash
# From repository root
cd /home/matador1081/semestre6/deploy/dsw02-practica01

# Start PostgreSQL and API containers
docker-compose -f docker/docker-compose.yml up -d

# Verify containers are running
docker ps | grep -E "(postgres|empleados)"
```

**Expected Output**:
```
CONTAINER ID   IMAGE                    PORTS
abc123def456   postgres:15              0.0.0.0:5433->5432/tcp
xyz789uvw012   empleados-api:latest     0.0.0.0:8080->8080/tcp
```

### Step 1.2: Wait for API Startup

```bash
# Poll until API is ready (may take 30–60 seconds)
until curl -s http://localhost:8080/actuator/health | grep '"status":"UP"' > /dev/null; do
  echo "Waiting for API startup..."
  sleep 5
done

echo "✅ API is ready!"
```

### Step 1.3: Verify Database

```bash
# Check if empleado records exist
docker exec -it <postgres_container_id> psql -U empleados_user -d empleados_db -c "SELECT correo_electronico, rol FROM empleado LIMIT 3;"

# Expected output (if data exists from migrations):
#  correo_electronico  | rol
# --------------------+------
#  admin@example.com  | ADMIN
#  user1@example.com  | USER
```

---

## 2. Authenticate with cURL

### Step 2.1: Test with Valid ADMIN Credentials

```bash
# ADMIN user: admin@example.com / admin123
# Encode credentials to Base64:
#   admin@example.com:admin123  →  YWRtaW5AZXhhbXBsZS5jb206YWRtaW4xMjM=

curl -X GET \
  http://localhost:8080/api/v2/empleados \
  -H "Authorization: Basic YWRtaW5AZXhhbXBsZS5jb206YWRtaW4xMjM=" \
  -H "Content-Type: application/json"

# Or use -u flag (cURL automatically encodes):
curl -X GET \
  http://localhost:8080/api/v2/empleados \
  -u "admin@example.com:admin123" \
  -H "Content-Type: application/json"
```

**Expected Response**:
```json
{
  "content": [
    {
      "id_empleado": 1,
      "nombre": "Admin",
      "apellido": "User",
      "correo_electronico": "admin@example.com",
      "rol": "ADMIN",
      "activo": true,
      "fecha_creacion": "2026-01-01T08:00:00Z"
    },
    {
      "id_empleado": 2,
      "nombre": "Regular",
      "apellido": "User",
      "correo_electronico": "user@example.com",
      "rol": "USER",
      "activo": true,
      "fecha_creacion": "2026-01-10T12:30:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "total_elements": 2,
  "total_pages": 1
}
```

### Step 2.2: Test with Invalid Credentials

```bash
curl -X GET \
  http://localhost:8080/api/v2/empleados \
  -u "admin@example.com:wrongpassword" \
  -H "Content-Type: application/json"
```

**Expected Response** (HTTP 401):
```json
{
  "error": "Unauthorized",
  "message": "Invalid email or password",
  "timestamp": "2026-03-17T10:30:15Z"
}
```

### Step 2.3: Test Without Credentials

```bash
curl -X GET \
  http://localhost:8080/api/v2/empleados \
  -H "Content-Type: application/json"
```

**Expected Response** (HTTP 401):
```
HTTP/1.1 401 Unauthorized
WWW-Authenticate: Basic realm="empleados-api"

{
  "error": "Unauthorized",
  "message": "Authorization header missing"
}
```

---

## 3. Test Rate Limiting (Brute-Force Protection)

### Step 3.1: Simulate 5 Failed Attempts

```bash
# Script: Attempt invalid login 5 times in quick succession
for i in {1..5}; do
  echo "Attempt $i:"
  curl -s -X GET \
    http://localhost:8080/api/v2/empleados \
    -u "admin@example.com:wrongpass" \
    -H "Content-Type: application/json" | jq '.message'
  sleep 1
done
```

**Expected Output**:
```
Attempt 1:
"Invalid email or password"
Attempt 2:
"Invalid email or password"
Attempt 3:
"Invalid email or password"
Attempt 4:
"Invalid email or password"
Attempt 5:
(HTTP 429 Too Many Requests)
```

### Step 3.2: 6th Attempt (Rate Limited)

```bash
curl -X GET \
  http://localhost:8080/api/v2/empleados \
  -u "admin@example.com:correctpassword" \
  -H "Content-Type: application/json"
```

**Expected Response** (HTTP 429):
```json
{
  "error": "TooManyRequests",
  "message": "Rate limit exceeded for account admin@example.com",
  "retry_after_seconds": 300
}
```

### Step 3.3: Wait for Lockout to Expire (Optional)

```bash
# Wait 5 minutes (300 seconds), then retry valid credentials
echo "Waiting for rate-limit lockout to expire (5 minutes)..."
sleep 300

curl -X GET \
  http://localhost:8080/api/v2/empleados \
  -u "admin@example.com:admin123" \
  -H "Content-Type: application/json" | jq '.content | length'

# Expected: Should return employee list (rate limit reset)
```

---

## 4. Swagger UI: Interactive Testing

### Step 4.1: Open Swagger UI

```bash
# Open in browser:
open http://localhost:8080/swagger-ui.html

# Or on Linux:
firefox http://localhost:8080/swagger-ui.html
```

### Step 4.2: Authorize with HTTP Basic

1. Click the green **"Authorize"** button (top right)
2. In the "basicAuth" section, enter:
   - **Username**: `admin@example.com`
   - **Password**: `admin123`
3. Click **"Authorize"** button
4. Close the modal

### Step 4.3: Try API Endpoints

**GET /api/v2/empleados** (List employees):
1. Click the GET endpoint
2. Click "Try it out"
3. (Optional) Add query params: `page=0&size=5`
4. Click "Execute"
5. View the response (should succeed)

**POST /api/v2/empleados** (Create new employee, ADMIN only):
1. Click POST endpoint
2. Click "Try it out"
3. Fill in Request Body:
```json
{
  "nombre": "Carlos",
  "apellido": "García López",
  "correo_electronico": "carlos.garcia@example.com",
  "rol": "USER"
}
```
4. Click "Execute"
5. Response: HTTP 201 Created with new employee

**Test Authorization (USER trying ADMIN operation)**:
1. Authorize with USER credentials instead (if available)
2. Try POST endpoint again
3. Response: HTTP 403 Forbidden ("USER role cannot create employees")

---

## 5. cURL Examples: Common Operations

### Create New Employee (ADMIN)

```bash
curl -X POST \
  http://localhost:8080/api/v2/empleados \
  -u "admin@example.com:admin123" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Carlos",
    "apellido": "García López",
    "correo_electronico": "carlos.garcia@example.com",
    "rol": "USER"
  }'
```

**Response** (HTTP 201):
```json
{
  "id_empleado": 42,
  "nombre": "Carlos",
  "apellido": "García López",
  "correo_electronico": "carlos.garcia@example.com",
  "rol": "USER",
  "activo": true,
  "fecha_creacion": "2026-03-17T10:30:00Z"
}
```

### Get Employee by ID

```bash
curl -X GET \
  http://localhost:8080/api/v2/empleados/42 \
  -u "admin@example.com:admin123" \
  -H "Content-Type: application/json"
```

### List Employees with Pagination

```bash
# Page 0, 10 items per page
curl -X GET \
  "http://localhost:8080/api/v2/empleados?page=0&size=10" \
  -u "admin@example.com:admin123" \
  -H "Content-Type: application/json" | jq '.content | length'
```

### Update Employee (PUT)

```bash
curl -X PUT \
  http://localhost:8080/api/v2/empleados/42 \
  -u "admin@example.com:admin123" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Carlos",
    "apellido": "García López",
    "rol": "ADMIN"
  }'
```

### Delete Employee (ADMIN)

```bash
curl -X DELETE \
  http://localhost:8080/api/v2/empleados/42 \
  -u "admin@example.com:admin123" \
  -H "Content-Type: application/json"

# Response: HTTP 204 No Content
```

---

## 6. Postman Collection (Alternative)

### Step 6.1: Import OpenAPI into Postman

1. Download Postman: https://www.postman.com/downloads/
2. In Postman, go to **"File" → "Import"**
3. Select _"Link"_ and enter:
   ```
   http://localhost:8080/v3/api-docs
   ```
4. Click "Import"

### Step 6.2: Set Environment Variables

1. Create new Environment: **"⚙️ Gear" → "Environments" → "Create New Environment"**
2. Add variables:
   ```
   base_url    = http://localhost:8080
   username    = admin@example.com
   password    = admin123
   ```
3. Select this environment (top right)

### Step 6.3: Test Endpoints

- All imported endpoints now use `{{base_url}}` placeholder
- **Authorization**: Select "Basic Auth" tab; use `{{username}}` and `{{password}}`
- Click "Send"

---

## 7. Troubleshooting

### API Not Starting

```bash
# Check logs
docker logs -f <api_container_id>

# Look for errors like:
# - "Connection refused" (PostgreSQL not ready)
# - "Port 8080 already in use" (kill process on port 8080)
#   lsof -i :8080 | grep -v COMMAND | awk '{print $2}' | xargs kill -9
```

### Authentication Fails (500 Error)

```bash
# Check if UserDetailsService is working
docker logs <api_container_id> | grep -i "authn\|user\|service"

# Verify database has empleado records
docker exec <postgres_container_id> psql -U empleados_user -d empleados_db \
  -c "SELECT COUNT(*) FROM empleado;"
```

### "Too Many Requests" Persists After 5 Min

```bash
# Restart API to clear in-memory rate-limit cache (MVP limitation)
docker restart <api_container_id>

# (Note: Production multi-instance will use distributed cache)
```

### Swagger UI Shows No Endpoints

```bash
# Check OpenAPI is generated
curl -s http://localhost:8080/v3/api-docs | jq '.paths | keys'

# If empty, check if SecurityConfig is applied
docker logs <api_container_id> | grep -i "security"
```

---

## 8. Testing Authenticated Flow Programmatically

### JavaScript (Browser/Node.js)

```javascript
const credentials = 'admin@example.com:admin123';
const encoded = btoa(credentials);

async function fetchEmployees() {
  const response = await fetch('http://localhost:8080/api/v2/empleados', {
    method: 'GET',
    headers: {
      'Authorization': `Basic ${encoded}`,
      'Content-Type': 'application/json'
    }
  });
  
  if (response.ok) {
    const data = await response.json();
    console.log('Employees:', data.content);
  } else {
    console.error(`Error ${response.status}: ${response.statusText}`);
  }
}

fetchEmployees();
```

### Python

```python
import requests
from requests.auth import HTTPBasicAuth

url = 'http://localhost:8080/api/v2/empleados'
auth = HTTPBasicAuth('admin@example.com', 'admin123')

response = requests.get(url, auth=auth)

if response.status_code == 200:
    employees = response.json()
    print(f"Found {len(employees['content'])} employees")
else:
    print(f"Error {response.status_code}: {response.text}")
```

### Java

```java
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class EmpleadosApiClient {
    public static void main(String[] args) throws Exception {
        String email = "admin@example.com";
        String password = "admin123";
        String credentials = email + ":" + password;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());

        HttpRequest request = HttpRequest.newBuilder()
            .uri(java.net.URI.create("http://localhost:8080/api/v2/empleados"))
            .header("Authorization", "Basic " + encoded)
            .GET()
            .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
            .send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Status: " + response.statusCode());
        System.out.println("Body: " + response.body());
    }
}
```

---

## 9. Clean Up

```bash
# Stop containers
docker-compose -f docker/docker-compose.yml down

# Remove volumes (optional, if you want to reset database)
docker-compose -f docker/docker-compose.yml down -v
```

---

## 10. Next Steps

- Read [spec.md](./spec.md) for detailed requirements
- Review [contracts/empleados-auth-v2.openapi.yaml](./contracts/empleados-auth-v2.openapi.yaml) for full endpoint documentation
- Check [data-model.md](./data-model.md) for authentication flow diagrams
- See [tasks.md](./tasks.md) for implementation tasks

