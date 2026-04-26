# Repository Layout

The repository starts as a modular monolith with clear boundaries.

## Root

- `backend/` - Spring Boot API and future worker runtime.
- `frontend/` - React TypeScript application shell.
- `docs/` - developer and operator documentation.
- `.github/workflows/` - CI quality gates.
- `docker-compose.yml` - local development stack.
- `AGENTS.md`, `CLAUDE.md`, `SKILLS.md` - AI coding-agent guidance.
- `FEATURES.md`, `ROADMAP.md`, `DOCSITE.md` - product, delivery, and documentation planning.

## Backend

- `com.clm.platform.api` - versioned REST boundary.
- `com.clm.platform.config` - application configuration.
- `com.clm.platform.domain` - domain services and models, added by later stories.
- `com.clm.platform.persistence` - persistence adapters and repositories.
- `com.clm.platform.security` - authentication and authorization components.
- `com.clm.platform.worker` - async task and worker components.

The only active backend behavior in R1-E01 is application startup, health/info exposure, persistence wiring, and a closed-by-default security filter chain.

## Frontend

- `src/App.tsx` - app shell routes.
- `src/main.tsx` - browser bootstrap.
- `src/styles.css` - foundation styling.
- `src/test/` - test setup.

The frontend intentionally contains no certificate lifecycle screens yet.
