# Agent Instructions

This file is for coding agents working in this repository.

The project is an enterprise certificate lifecycle management platform with a Spring Boot backend and React frontend. It is inspired by Netflix Lemur concepts, but should be implemented as a modern, production-grade product rather than a direct port.

## Read Order

Load only what is needed for the current task.

1. Read this file first.
2. Read the specific epic and story in [ROADMAP.md](ROADMAP.md).
3. Read only the relevant sections of [FEATURES.md](FEATURES.md).
4. Read [SKILLS.md](SKILLS.md) only for the skill card matching your task.
5. Read [DOCSITE.md](DOCSITE.md) when the task changes user-facing behavior, APIs, operations, or integrations.

Do not load the full roadmap and feature catalog into context for small code changes.

## Current State

No application code exists yet. The root files define product scope, roadmap, architecture guidance, docsite strategy, and agent workflow.

Do not add application code unless the user explicitly asks for implementation.

## Target Stack

Verify exact versions when implementation begins, but assume this direction:

- Backend: Java 21 LTS or newer project-approved LTS, Spring Boot 3.x, Spring Security, Spring Data JPA, Flyway, PostgreSQL.
- Async work: internal task abstraction first, with queue backends pluggable later.
- Frontend: React, TypeScript, Vite or project-approved equivalent, modern routing, data-fetching cache, accessible component patterns.
- API: REST under `/api/v1`, OpenAPI generated from source, stable error model.
- Docs: product docsite generated from markdown or MDX, with API reference generated from OpenAPI.
- Deployment: Docker, Compose for local dev, Helm for Kubernetes.

Do not pin dependency versions from memory. Check current official docs or package metadata when coding begins.

## Architectural Direction

Start as a modular monolith with strong boundaries. Split services later only when real scaling or ownership pressure exists.

Backend module boundaries should map to product domains:

- identity and access
- tenants and organizations
- certificates and versions
- authorities, issuers, and profiles
- requests and approvals
- sources and discovery
- endpoints
- destinations and deployments
- automation and tasks
- notifications
- policy and risk
- plugins
- audit
- reporting
- admin configuration
- MCP server integration later

Each domain should expose application services and DTOs. Avoid direct cross-domain repository access from unrelated modules.

## Domain Invariants

Preserve these invariants unless a story explicitly changes them:

- Every certificate must have an owner or an explicit orphaned state.
- Every sensitive action must be authorized and audited.
- Private key material must never be logged.
- Private key export must be disabled by default and gated by permission, policy, reason, and audit.
- Async lifecycle operations must be idempotent.
- Certificate versions are append-oriented. Avoid mutating historical certificate facts.
- Discovery may create observations, but managed lifecycle state must be changed through explicit services.
- Sources observe or import. Destinations deploy. Issuers issue. Keep these responsibilities separate.
- Plugin failures must not crash the platform or corrupt core state.
- Tenant isolation must be enforced in queries, APIs, jobs, and UI.

## API Conventions

Use consistent REST patterns:

- Base path: `/api/v1`.
- Resource names: plural nouns.
- Pagination: explicit page or cursor contract chosen early and used consistently.
- Filtering: documented fields, typed validation, safe defaults.
- Errors: machine-readable `code`, human message, remediation hint where useful, correlation ID, and docs link.
- Async operations: return task ID and resource links.
- Bulk operations: support dry run, validation report, partial success where safe, and downloadable error details.
- Sensitive operations: require reason field and emit audit event.

Never return private keys, secrets, bearer tokens, connector credentials, or raw decrypted secret values from normal APIs.

## Backend Coding Rules

When code exists:

- Keep controllers thin. Put business rules in application services.
- Use DTOs for API boundaries. Do not expose JPA entities directly.
- Use migrations for every schema change.
- Add database indexes for list filters and joins introduced by a story.
- Prefer explicit transaction boundaries.
- Validate inputs at API and service boundaries.
- Use typed domain exceptions mapped to stable API error codes.
- Redact sensitive values in logs and exception messages.
- Put crypto parsing and validation behind focused services with tests.
- Do not hand-roll security-sensitive crypto primitives.

## Frontend Coding Rules

When code exists:

- Build the actual app experience, not a landing page.
- Use dense, operational UI patterns suitable for security and infrastructure teams.
- Prefer tables, filters, split panes, timelines, drawers, dialogs, tabs, forms, and topology views over decorative cards.
- Keep cards for repeated items, modals, and framed tools only.
- Use icons for tool buttons where available.
- Ensure text fits on mobile and desktop.
- Implement loading, empty, error, and permission-denied states for every route.
- Keep accessibility in scope: labels, keyboard flow, focus, contrast, and semantic controls.

## Security Requirements

Every implementation story must consider:

- Authentication and authorization.
- Tenant isolation.
- Audit events.
- Secret redaction.
- Private key handling.
- CSRF/CORS/session/token behavior where relevant.
- Rate limits for sensitive or expensive endpoints.
- Dependency and supply-chain impact.
- Abuse cases for user-controlled names, plugin options, URLs, and file uploads.

For certificate and key handling, add negative tests for malformed input, mismatched key, weak algorithms, expired chains, and unauthorized access.

## Testing Expectations

Use the smallest test that proves the behavior, but do not skip risk-based coverage.

Backend:

- Unit tests for pure services, validators, policy checks, and parsers.
- Integration tests for repositories, migrations, API authorization, and async task behavior.
- Contract tests for plugin interfaces and public APIs.
- Security tests for permissions and tenant isolation.

Frontend:

- Component tests for tables, forms, detail views, dialogs, and route guards.
- Workflow tests for core user flows.
- Accessibility checks for interactive workflows where feasible.

End-to-end:

- Use sparingly for critical journeys: login, import certificate, view detail, request certificate, approval, renewal, deployment, and audit evidence.

Performance:

- Add performance checks when stories introduce inventory list queries, scans, reports, dashboards, or bulk operations.

## Documentation Requirements

If a story changes behavior, update docs in the same change.

Documentation types:

- Product docs for user workflows.
- Admin docs for configuration and identity.
- Operator docs for deployment, jobs, and troubleshooting.
- Developer docs for APIs, plugins, and extension points.
- Security docs for threat model, audit, key handling, and permissions.

Use [DOCSITE.md](DOCSITE.md) for page structure and templates.

## Story Execution Workflow

For a coding story:

1. Identify the exact story ID from [ROADMAP.md](ROADMAP.md).
2. Read the matching feature sections from [FEATURES.md](FEATURES.md).
3. Select the matching skill card from [SKILLS.md](SKILLS.md).
4. Inspect existing code before editing.
5. Implement the smallest vertical slice that satisfies acceptance criteria.
6. Add tests and docs.
7. Run relevant validation commands.
8. Summarize changed files, tests run, and remaining risks.

Do not bundle unrelated stories into one change.

## Commit And PR Guidance

Use story IDs in branch names, commits, and PR titles when possible.

Suggested format:

- Branch: `feature/R1-E04-S01-import-pem-certificate`
- Commit: `R1-E04-S01 import PEM certificates`
- PR title: `R1-E04-S01: Import PEM certificates`

PR body should include:

- Story ID.
- User-visible behavior.
- Security considerations.
- Tests run.
- Docs updated.
- Migration notes if any.

## Review Checklist

Before finalizing a coding task, check:

- Does it satisfy the story acceptance criteria?
- Did it preserve domain invariants?
- Are permissions and tenant scope tested?
- Are audit events emitted for sensitive changes?
- Are secrets redacted?
- Are list queries paginated and indexed?
- Are async operations idempotent?
- Is the UI accessible and responsive?
- Are docs and OpenAPI updated?
- Were relevant tests run?

## Things To Avoid

- Do not implement a direct Lemur port.
- Do not store raw private keys in plain database fields.
- Do not expose entities directly as API responses.
- Do not make plugin code able to bypass authorization or secret controls.
- Do not create decorative frontend pages instead of usable workflows.
- Do not add large framework choices without a story-driven reason.
- Do not skip docs for user-visible behavior.
- Do not use memory for current dependency versions when exact versions matter.
