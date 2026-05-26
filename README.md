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

## Deployment (Fly.io)

The backend ships as a container on [Fly.io](https://fly.io) with managed Postgres
(ADR-0001 auth, ADR-0002 Liquibase-owned schema). The `Dockerfile` (multi-stage Maven
build → distroless JRE) and `fly.toml` are committed; the steps below are run once,
interactively, against your Fly account.

### Environment variables

On the `docker` profile the app reads its datasource and credentials from the
environment. On Fly these are stored as secrets (encrypted, injected at runtime):

| Variable                     | Description                                  |
|------------------------------|----------------------------------------------|
| `SPRING_PROFILES_ACTIVE`     | Must be `docker` (already set in `fly.toml`)  |
| `SPRING_DATASOURCE_URL`      | JDBC URL, e.g. `jdbc:postgresql://<host>:5432/<db>` |
| `SPRING_DATASOURCE_USERNAME` | Postgres username                            |
| `SPRING_DATASOURCE_PASSWORD` | Postgres password                            |
| `APP_USERNAME`               | HTTP Basic username (no dev default in prod) |
| `APP_PASSWORD`               | HTTP Basic password (no dev default in prod) |

`fly postgres attach` injects a `DATABASE_URL` like
`postgres://user:pass@host:5432/db`. Spring needs it split into the three
`SPRING_DATASOURCE_*` values above, with the URL rewritten to the
`jdbc:postgresql://host:5432/db` form.

### Build and run the container locally

```bash
# Build the image (BuildKit required; on by default in modern Docker)
docker build -t fittracker .

# Run against a local Postgres (e.g. the one in docker-compose.yml)
docker run --rm -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/fittracker \
  -e SPRING_DATASOURCE_USERNAME=fittracker \
  -e SPRING_DATASOURCE_PASSWORD=fittracker \
  -e APP_USERNAME=me -e APP_PASSWORD=s3cret \
  fittracker
```

### First deploy to Fly

```bash
fly launch --no-deploy        # set `app` + `primary_region` in fly.toml
fly postgres create           # provision managed Postgres
fly postgres attach <pg-app>  # wire it to this app (sets a DATABASE_URL secret)
```

`attach` only sets `DATABASE_URL` (in `postgres://user:pass@host:5432/db?sslmode=disable`
form). Set the `SPRING_DATASOURCE_*` values Spring actually reads from it — keep
`?sslmode=disable` (the private `.flycast` network is plaintext). Run `secrets set` as a
**single line** with each value **quoted** (multi-line `\` continuations break on paste in
some terminals, and an unquoted `?` is glob-expanded by zsh):

```bash
fly secrets set SPRING_DATASOURCE_URL='jdbc:postgresql://<host>:5432/<db>?sslmode=disable' SPRING_DATASOURCE_USERNAME='<user>' SPRING_DATASOURCE_PASSWORD='<pass>' APP_USERNAME='<you>' APP_PASSWORD='<strong-password>'
```

```bash
fly deploy
```

Fly polls `GET /actuator/health` (permitted without auth) to gate the rollout.
Verify the public URL once deployed:

```bash
curl -i https://<app>.fly.dev/api/v1/cycles            # 401 without credentials
curl -u <you>:<password> https://<app>.fly.dev/api/v1/cycles  # 200
```

> The `fly.toml` VM is 512 MB — a JVM tends to OOM at the free-tier 256 MB. Drop it
> only if you also constrain the heap.
