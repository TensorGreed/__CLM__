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

The worker service currently runs the same Spring Boot image with the `worker` profile. R1-E02 will add the task abstractions that make this process do real async work.

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
