# CLM Platform

This repository is being shaped into an enterprise certificate lifecycle management platform with a Spring Boot backend and React frontend.

## Implementation Status

R1-E01 and R1-E02 are now implemented as the initial repository, build, and backend platform foundation:

- Spring Boot backend skeleton in [backend](backend).
- React TypeScript frontend shell in [frontend](frontend).
- Local PostgreSQL development stack in [docker-compose.yml](docker-compose.yml).
- Baseline CI in [.github/workflows/ci.yml](.github/workflows/ci.yml).
- Developer and operator setup docs in [docs](docs).
- `/api/v1` backend foundation with OpenAPI, stable error envelopes, correlation IDs, audit events, and async task runs.

No certificate lifecycle business features have been added yet.

Start with these root planning files:

- [FEATURES.md](FEATURES.md) - product vision, feature catalog, domain model, integration backlog, and nonfunctional requirements.
- [ROADMAP.md](ROADMAP.md) - implementation roadmap in epics and agent-ready user stories.
- [AGENTS.md](AGENTS.md) - engineering guidance for coding agents.
- [CLAUDE.md](CLAUDE.md) - concise Claude-specific project instructions.
- [SKILLS.md](SKILLS.md) - reusable AI skill cards for product, backend, frontend, testing, security, plugin, docs, and MCP work.
- [DOCSITE.md](DOCSITE.md) - product documentation site strategy and page templates.

## Quick Start

Run the full local stack:

```bash
docker compose up --build
```

Open:

- Frontend: `http://localhost:5173`
- Backend health: `http://localhost:8080/actuator/health`
- Backend API root: `http://localhost:8080/api/v1`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui`
- PostgreSQL: `localhost:5432`

Start the optional worker profile:

```bash
docker compose --profile worker up --build
```

## Local Validation

Backend:

```bash
cd backend
./mvnw test
```

Frontend:

```bash
cd frontend
npm ci
npm run lint
npm test
npm run build
```

Documentation:

```bash
npm ci
npm run docs:lint
```

## More Setup Detail

- [Developer setup](docs/development/setup.md)
- [Repository layout](docs/development/repository-layout.md)
- [Backend foundation](docs/development/backend.md)
- [API conventions](docs/development/api-conventions.md)
- [Schema and migrations](docs/development/schema.md)
- [Audit foundation](docs/development/audit.md)
- [Worker foundation](docs/development/workers.md)
- [Observability foundation](docs/development/observability.md)
- [Frontend foundation](docs/development/frontend.md)
- [Local Docker Compose](docs/operations/local-compose.md)
- [Contributing](docs/development/contributing.md)
