# Project Skill Cards

These are lightweight, repo-local skill cards for future AI coding agents. They are designed to reduce token usage by telling agents what to read, what to produce, and what to avoid.

This file is not an auto-installed Codex skill directory. If the team later wants executable Codex skills, convert individual cards into dedicated `SKILL.md` folders.

## Skill: story-slicer

Use when turning product scope into implementation-ready issues.

Read:

- [ROADMAP.md](ROADMAP.md) for target epic and neighboring dependencies.
- [FEATURES.md](FEATURES.md) only for relevant capability sections.
- [DOCSITE.md](DOCSITE.md) if user-facing docs are affected.

Output:

- Story ID and title.
- Goal and non-goals.
- API/UI/data changes.
- Security and audit considerations.
- Acceptance criteria.
- Test plan.
- Docs impact.
- Dependencies and rollout notes.

Guardrails:

- Keep stories independently shippable.
- Do not mix backend foundation, UI polish, connector work, and policy changes unless required for a vertical slice.
- Prefer one domain boundary per story.

## Skill: backend-spring

Use when implementing backend APIs, domain services, persistence, workers, security, or integrations.

Read:

- [AGENTS.md](AGENTS.md) sections: Architectural Direction, Domain Invariants, API Conventions, Backend Coding Rules, Security Requirements, Testing Expectations.
- Exact story in [ROADMAP.md](ROADMAP.md).
- Relevant domain section in [FEATURES.md](FEATURES.md).

Output:

- Spring Boot implementation scoped to the story.
- Database migration if schema changes.
- Service and API tests.
- OpenAPI update.
- Audit and permission tests for sensitive actions.
- Docs update for changed behavior.

Guardrails:

- Do not expose JPA entities as API DTOs.
- Do not bypass authorization in workers.
- Do not log secrets or private key material.
- Do not invent plugin contracts without checking plugin roadmap sections.
- Keep async work idempotent.

## Skill: frontend-react

Use when implementing React UI, routes, forms, tables, dashboards, or operational workflows.

Read:

- [AGENTS.md](AGENTS.md) sections: Frontend Coding Rules and Documentation Requirements.
- Exact story in [ROADMAP.md](ROADMAP.md).
- UI Experience section in [FEATURES.md](FEATURES.md).

Output:

- User-facing workflow that satisfies the story.
- Component or workflow tests.
- Responsive layout.
- Loading, empty, error, and permission-denied states.
- Accessibility basics.
- Docs screenshots or page notes when docsite exists.

Guardrails:

- Do not build landing pages inside the app.
- Do not use oversized hero sections, decorative card-heavy layouts, or marketing copy for operational features.
- Do not hide important status behind hover-only UI.
- Do not create UI actions that backend policy cannot enforce.

## Skill: certificate-crypto

Use when parsing, validating, importing, issuing, renewing, revoking, or exporting certificates and keys.

Read:

- [FEATURES.md](FEATURES.md) sections: Certificate Inventory, Issuance, Renewal And Rotation, Export Formats, Security.
- [AGENTS.md](AGENTS.md) sections: Domain Invariants and Security Requirements.
- Exact certificate story in [ROADMAP.md](ROADMAP.md).

Output:

- Safe parsing and validation through well-tested libraries.
- Tests for PEM, DER, chain, CSR, SANs, EKUs, fingerprints, malformed inputs, and key mismatch where relevant.
- Redaction rules for logs and errors.
- Audit events for sensitive operations.
- Docs for accepted formats and troubleshooting.

Guardrails:

- Do not hand-roll cryptographic primitives.
- Do not store private keys in plaintext.
- Do not expose private keys in normal API responses.
- Do not mutate historical certificate facts on old versions.

## Skill: plugin-connector

Use when designing or implementing issuer, source, destination, notification, export, DNS, policy, credential, ownership, metric, or MCP plugin work.

Read:

- [FEATURES.md](FEATURES.md) sections: Plugins, Integration Backlog, Destinations, Discovery, Notifications, Export Formats.
- [ROADMAP.md](ROADMAP.md) epics R3-E01, R3-E03, R5-E01, and the exact story.
- [AGENTS.md](AGENTS.md) plugin and security rules.

Output:

- Plugin contract or connector implementation.
- Option schema with validation and docs.
- Permission and secret requirements.
- Health and preflight behavior.
- Contract tests and fake provider tests.
- Connector docs with prerequisites, permissions, config, examples, error codes, and troubleshooting.

Guardrails:

- Keep source, destination, issuer, and export responsibilities separate.
- Treat connector credentials as secret references.
- Add timeouts, retries, redaction, and typed errors.
- Do not let plugin code bypass core authorization or audit.

## Skill: security-review

Use for security reviews, threat modeling, auth changes, key handling, plugin execution, MCP, and sensitive workflows.

Read:

- [AGENTS.md](AGENTS.md) Security Requirements and Domain Invariants.
- [FEATURES.md](FEATURES.md) Security, Identity And Access, Policy And Compliance, Plugins, MCP Server.
- Relevant story in [ROADMAP.md](ROADMAP.md).

Output:

- Findings ordered by severity.
- Affected file and line references when code exists.
- Exploit or failure scenario.
- Required fix.
- Test gap.
- Residual risk.

Guardrails:

- Prioritize private key exposure, tenant data leakage, auth bypass, plugin escape, SSRF, secret logging, and unsafe deserialization.
- Do not accept "UI hides the button" as authorization.
- Verify workers enforce the same permissions and tenant scope as APIs.

## Skill: test-engineer

Use when adding or reviewing tests, quality gates, CI, fixtures, or end-to-end coverage.

Read:

- [AGENTS.md](AGENTS.md) Testing Expectations and Review Checklist.
- Story acceptance criteria in [ROADMAP.md](ROADMAP.md).
- Relevant feature section in [FEATURES.md](FEATURES.md).

Output:

- Test plan mapped to acceptance criteria.
- Unit, integration, contract, security, UI, or e2e tests as appropriate.
- CI command updates where needed.
- Notes on what was not tested and why.

Guardrails:

- Prefer focused tests over broad brittle tests.
- Add negative authorization and tenant isolation tests for sensitive stories.
- Add malformed input tests for certificate parsing and plugin option handling.
- Do not fake away the behavior the story is meant to prove.

## Skill: docs-writer

Use when creating or updating product docs, admin docs, operator runbooks, developer docs, API docs, plugin docs, or MCP docs.

Read:

- [DOCSITE.md](DOCSITE.md).
- Relevant story in [ROADMAP.md](ROADMAP.md).
- Relevant feature section in [FEATURES.md](FEATURES.md).

Output:

- User-centered documentation page.
- Prerequisites, steps, expected result, troubleshooting, permissions, and security notes.
- API examples or CLI examples once they exist.
- Links to related concepts and runbooks.

Guardrails:

- Do not document behavior that does not exist.
- Keep docs task-oriented.
- Put troubleshooting near the workflow that fails.
- Tie error codes to remediation steps once error codes exist.

## Skill: devops-platform

Use for Docker, Compose, Helm, Kubernetes, config, CI/CD, observability, backup, restore, upgrade, and release work.

Read:

- [FEATURES.md](FEATURES.md) Deployment, Observability, Reliability, Operability.
- [ROADMAP.md](ROADMAP.md) epics R1-E01 and R4-E04.
- [DOCSITE.md](DOCSITE.md) operator docs sections.

Output:

- Reproducible local and production deployment changes.
- Config reference updates.
- Health checks, probes, metrics, and logs.
- Backup, restore, upgrade, and rollback notes when relevant.
- CI validation.

Guardrails:

- Do not bake secrets into images or manifests.
- Validate unsafe defaults.
- Keep dev convenience separate from production defaults.
- Document operational failure modes.

## Skill: mcp-designer

Use when designing or implementing the future MCP server, MCP tools, MCP resources, and AI-safe CLM workflows.

Read:

- [FEATURES.md](FEATURES.md) MCP Server, API And CLI, Security, Identity And Access.
- [ROADMAP.md](ROADMAP.md) epic R5-E03.
- [AGENTS.md](AGENTS.md) Security Requirements.

Output:

- Tool/resource/prompt contract.
- Required API scopes.
- Redaction behavior.
- Approval and reason-capture behavior.
- Security tests for RBAC and secret suppression.
- MCP docs.

Guardrails:

- MCP must call public APIs with scoped credentials.
- MCP must not read the database directly.
- MCP must not expose private keys, connector credentials, tokens, or raw secrets.
- Action tools must respect approval policy and audit requirements.

## Skill: product-docsite

Use when shaping the public or internal documentation site.

Read:

- [DOCSITE.md](DOCSITE.md).
- [FEATURES.md](FEATURES.md) Documentation Site and relevant feature sections.
- [ROADMAP.md](ROADMAP.md) story docs column.

Output:

- Information architecture.
- Page templates.
- Tutorial sequence.
- API and plugin reference structure.
- Release note and migration structure.

Guardrails:

- Do not make docs an afterthought after code.
- Every feature should have operator and troubleshooting coverage.
- Keep generated API docs tied to OpenAPI.

## Skill: product-competitive

Use when evaluating feature parity or product differentiation against Lemur, DigiCert, Keyfactor, Smallstep, Venafi, cert-manager, Vault PKI, or other CLM/PKI tools.

Read:

- [FEATURES.md](FEATURES.md) Reference Baseline, Product Principles, Feature Catalog, Integration Backlog.
- Current official vendor documentation when making current-market claims.

Output:

- Capability matrix.
- Differentiators.
- Gaps and roadmap recommendations.
- Risks and dependencies.

Guardrails:

- Browse current official sources before making time-sensitive competitive claims.
- Distinguish confirmed vendor features from inferred product strategy.
- Avoid copying competitor text or UX.
