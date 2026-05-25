# FitTracker

A single-user bodybuilding workout tracker. Spring Boot 4 (Java 21) REST API.

See `CONTEXT.md` for the domain model and `docs/adr/` for architecture decisions.

## Authentication

The API is gated by HTTP Basic auth for a single operator (ADR-0001). Credentials
come from environment variables, BCrypt-encoded at startup:

| Variable       | Description            | Dev default |
|----------------|------------------------|-------------|
| `APP_USERNAME` | Basic-auth username    | `admin`     |
| `APP_PASSWORD` | Basic-auth password    | `admin`     |

The dev defaults exist only so the app starts without configuration on the local
H2 profile. **Set both variables explicitly before exposing the app anywhere.**

All `/api/**` endpoints require authentication. On the default dev profile the
Swagger UI (`/swagger-ui.html`, `/v3/api-docs`) and H2 console (`/h2-console`) are
open for convenience; on the `docker` (Postgres) profile they require auth too.

## Running

```bash
# Dev — in-memory H2, default admin/admin credentials
./mvnw spring-boot:run

# With explicit credentials
APP_USERNAME=me APP_PASSWORD=s3cret ./mvnw spring-boot:run

# Docker/Postgres profile
APP_USERNAME=me APP_PASSWORD=s3cret \
  ./mvnw spring-boot:run -Dspring-boot.run.profiles=docker
```

The API listens on `http://localhost:8080`. Example authenticated request:

```bash
curl -u admin:admin http://localhost:8080/api/v1/cycles
```

## Tests

```bash
./mvnw test
```
