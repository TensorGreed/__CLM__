# Local Docker Compose

Use Docker Compose for the local development stack.

## Start The Core Stack

```bash
docker compose up --build
```

This starts:

- PostgreSQL on `localhost:5432`.
- Backend API on `http://localhost:8080`.
- Frontend UI on `http://localhost:5173`.

## Start With Worker Profile

```bash
docker compose --profile worker up --build
```

The worker service currently runs the same Spring Boot image with the `worker` profile and shares the platform task abstractions.

## Bootstrap Local Admin

After the API starts, check bootstrap status:

```bash
curl http://localhost:8080/api/v1/bootstrap/status
```

Create the local admin once:

```bash
curl -X POST http://localhost:8080/api/v1/bootstrap/admin \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.test","displayName":"Admin","password":"change-this-password","tenantSlug":"default","tenantName":"Default Tenant","organizationSlug":"platform","organizationName":"Platform Operations"}'
```

Use `docker compose down -v` when you need to erase the local database and re-run bootstrap.

## Stop And Remove Local State

```bash
docker compose down
```

Remove database and dependency volumes when you need a clean local environment:

```bash
docker compose down -v
```

## Environment

Copy `.env.example` to `.env` to override local database values.

The defaults are safe for local development only.
