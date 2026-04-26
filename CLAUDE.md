# Claude Project Instructions

This repository is for an enterprise certificate lifecycle management platform built with Spring Boot and React.

No application code exists yet. Do not add code unless the user asks for implementation.

## Read Minimal Context

1. Start with [AGENTS.md](AGENTS.md).
2. Read the exact story in [ROADMAP.md](ROADMAP.md).
3. Read only relevant sections of [FEATURES.md](FEATURES.md).
4. Use [SKILLS.md](SKILLS.md) to choose a task-specific skill card.
5. Use [DOCSITE.md](DOCSITE.md) whenever behavior, APIs, deployment, integrations, or troubleshooting changes.

Avoid loading every planning file for narrow tasks.

## Product Direction

Build a modern CLM platform inspired by Netflix Lemur concepts:

- certificates and versions
- authorities and issuers
- sources and discovery
- endpoints
- destinations and deployment
- notifications
- exports
- plugins
- RBAC
- audit logs
- scheduled automation

The target product should exceed Lemur in production readiness, enterprise governance, connector safety, UI quality, documentation, and future MCP support.

## Engineering Rules

- Use Spring Boot and React only when implementation starts.
- Verify current dependency versions before pinning them.
- Keep backend modules aligned to product domains.
- Use DTOs at API boundaries.
- Keep controllers thin and domain services explicit.
- Use migrations for schema changes.
- Treat private keys and connector credentials as highly sensitive.
- Enforce tenant scope and RBAC in every API and worker path.
- Audit sensitive actions.
- Add tests and docs with the implementation.

## UI Rules

- Build operational workflows, not marketing pages.
- Use dense, modern security-operations UI patterns.
- Include loading, empty, error, and permission-denied states.
- Keep accessibility, responsive layout, and text fit in scope.

## Security Rules

- Never log private keys, secrets, tokens, or decrypted connector credentials.
- Private key export is disabled by default and must require permission, policy, reason, and audit.
- Async lifecycle operations must be idempotent.
- Plugin failures must be isolated.
- MCP tools must be RBAC-aware and must not expose secrets.

## Response Style

When completing implementation work, summarize:

- story ID
- changed files
- tests run
- docs updated
- residual risks or follow-up stories
