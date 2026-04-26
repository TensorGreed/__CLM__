# CLM Platform Roadmap

## How To Use This Roadmap

Each story is written so a coding agent can pick it up with minimal extra context.

Story fields:

- ID: stable identifier for planning, branches, commits, tests, and docs.
- User story: actor, goal, and outcome.
- Acceptance criteria: externally visible behavior that must be true.
- Test scope: minimum expected automated or manual verification.
- Docs: product docsite or developer documentation impact.
- Dependencies: earlier story or epic prerequisites.

Status is tracked at the epic level below. Story tables remain stable so coding agents can reference IDs consistently.

Default definition of done for every story:

- Backend behavior has unit, integration, and security tests appropriate to risk.
- Frontend behavior has component and workflow tests appropriate to risk.
- API changes update OpenAPI and examples.
- Database changes use migrations and include rollback notes.
- Async behavior is idempotent and observable.
- Permission checks and audit events are explicit.
- Logs redact secrets and private key material.
- Errors use stable machine-readable codes.
- User-facing workflows update docsite pages.

## Release Strategy

- R0: Planning and architecture artifacts.
- R1: Core platform foundation and certificate inventory MVP.
- R2: Issuance, import, notifications, and basic automation.
- R3: Sources, destinations, endpoints, and verified rotation.
- R4: Enterprise governance, compliance, scale, and high availability.
- R5: Plugin SDK, connector ecosystem, AI/MCP server, and market-leading automation.

## Current Status

Last updated: 2026-04-26.

| Epic | Status | Completed Scope | Validation | Notes |
| --- | --- | --- | --- | --- |
| R0-E01: Product And Architecture Foundation | Done | Product feature catalog, roadmap, AI-agent guidance, Claude guidance, skill cards, and docsite strategy. | Documentation review and markdown lint. | Planning artifacts are intentionally root-level and agent-readable. |
| R1-E01: Repository And Build Foundation | Done | Spring Boot backend skeleton, React TypeScript frontend shell, local Compose stack, baseline CI, dependency automation, docs tooling, and developer/operator docs. | Backend tests, frontend lint/tests/build, docs lint, and Compose config validation passed. | `docker compose up` could not be runtime-verified until Docker Desktop Linux engine is running locally. |
| R1-E02: Backend Platform Core | Done | `/api/v1` foundation, OpenAPI generation, stable error envelope, query primitives, Flyway baseline migration, JSON logs with correlation IDs, audit event model/service, async task model/runner, and task detail API. | Backend tests, docs lint, Compose config, Compose startup, API health, API root, OpenAPI JSON, and UI probes passed. | No certificate business endpoints, auth/RBAC, plugins, or MCP were added. |
| R1-E03: Identity, Tenancy, And RBAC | Done | First-run bootstrap admin, local Basic auth, optional OIDC login with group-role mapping, tenant/org APIs with scope checks, built-in roles/permissions, sensitive-action reason capture, service accounts, and scoped API tokens. | Backend tests, docs lint, Compose config, Compose rebuild/startup, API health, API root, bootstrap status, OpenAPI JSON, protected API 401, and UI probe passed. | No certificate inventory, user-management UI, SCIM, SAML, ABAC, plugins, or MCP were added. |
| R1-E04: Certificate Inventory Core | In Progress | Backend API slice for R1-E04-S01, R1-E04-S02, and the backend portion of R1-E04-S03: PEM-only certificate import, certificate/version records, parsed metadata, fingerprint deduplication, tenant-scoped search, detail API, RBAC, and import audit events. | Backend tests, migration tests, docs lint, Compose config, Compose rebuild/startup, API health, API root, OpenAPI JSON, protected certificate API 401, and UI probe passed. | Frontend inventory screens, source observation deduplication, editable custom metadata, full status history, private keys, issuance, renewal, destinations, plugins, and MCP remain out of scope. |

## Epic R0-E01: Product And Architecture Foundation

Goal: Establish the product, architecture, and implementation contracts before coding.

| ID | User Story | Acceptance Criteria | Test Scope | Docs | Dependencies |
| --- | --- | --- | --- | --- | --- |
| R0-E01-S01 | As a product owner, I want a stable feature catalog so teams build toward the same CLM product. | FEATURES.md defines personas, domains, capabilities, integrations, and nonfunctional requirements. | Documentation review. | Root planning docs. | None |
| R0-E01-S02 | As an architect, I want an agent-ready roadmap so implementation can proceed in small slices. | ROADMAP.md contains epics, stories, acceptance criteria, tests, docs, and dependencies. | Documentation review. | Root planning docs. | R0-E01-S01 |
| R0-E01-S03 | As a lead engineer, I want agent instructions so code generation follows project standards. | AGENTS.md and CLAUDE.md define stack, workflow, test, security, and documentation rules. | Documentation review. | Agent docs. | R0-E01-S02 |
| R0-E01-S04 | As a platform lead, I want reusable AI skill cards so future agents load only relevant context. | SKILLS.md defines targeted skills and reading scopes. | Documentation review. | Agent docs. | R0-E01-S03 |
| R0-E01-S05 | As a docs owner, I want a docsite strategy so product documentation is designed from the first release. | DOCSITE.md defines IA, templates, authoring rules, and release docs. | Documentation review. | Docsite planning. | R0-E01-S01 |

## Epic R1-E01: Repository And Build Foundation

Goal: Create the Spring Boot, React, and documentation skeleton without premature business complexity.

| ID | User Story | Acceptance Criteria | Test Scope | Docs | Dependencies |
| --- | --- | --- | --- | --- | --- |
| R1-E01-S01 | As a developer, I want a multi-module repo layout so backend, frontend, workers, docs, and tooling have clear boundaries. | Repo has documented module layout and build commands. | CI smoke test. | Developer setup. | R0 |
| R1-E01-S02 | As a backend developer, I want a Spring Boot service skeleton so APIs, persistence, security, and async jobs can be added consistently. | Backend starts locally, exposes health endpoints, and has baseline test setup. | Unit and context load tests. | Backend setup. | R1-E01-S01 |
| R1-E01-S03 | As a frontend developer, I want a React TypeScript app shell so UI features share routing, auth state, and design tokens. | Frontend starts locally and has app shell, routing, test setup, and linting. | Component smoke tests. | Frontend setup. | R1-E01-S01 |
| R1-E01-S04 | As an operator, I want local Docker Compose so the platform can run with Postgres and supporting services. | Compose starts API, UI, DB, and local worker profile. | Local smoke test. | Local install. | R1-E01-S02 |
| R1-E01-S05 | As a maintainer, I want CI quality gates so broken code does not land. | CI runs backend tests, frontend tests, lint, formatting checks, dependency scan, and docs build. | CI pipeline. | Contribution guide. | R1-E01-S02, R1-E01-S03 |

## Epic R1-E02: Backend Platform Core

Goal: Establish backend patterns for APIs, persistence, errors, auditing, and async work.

| ID | User Story | Acceptance Criteria | Test Scope | Docs | Dependencies |
| --- | --- | --- | --- | --- | --- |
| R1-E02-S01 | As an API consumer, I want versioned REST endpoints so integrations can depend on stable contracts. | `/api/v1` base path, OpenAPI generation, pagination, sorting, filtering, and stable error envelope exist. | API contract tests. | API conventions. | R1-E01-S02 |
| R1-E02-S02 | As a developer, I want database migrations so schema changes are repeatable. | Flyway configured, baseline migration applied, and test DB migration verified. | Migration integration tests. | Schema guide. | R1-E01-S02 |
| R1-E02-S03 | As an operator, I want structured logs and correlation IDs so requests and jobs can be traced. | API and worker logs include request ID, actor, tenant, resource where safe, and redact sensitive fields. | Log assertions. | Observability guide. | R1-E02-S01 |
| R1-E02-S04 | As a security auditor, I want immutable audit events for sensitive actions. | Audit event model and service exist with actor, action, resource, decision, status, and timestamp. | Repository and service tests. | Audit guide. | R1-E02-S02 |
| R1-E02-S05 | As a platform engineer, I want async task execution so long lifecycle operations do not block API requests. | Task model, task runner abstraction, retry state, and task detail API exist. | Worker integration tests. | Worker guide. | R1-E02-S01 |

## Epic R1-E03: Identity, Tenancy, And RBAC

Goal: Secure access to certificate operations from the start.

| ID | User Story | Acceptance Criteria | Test Scope | Docs | Dependencies |
| --- | --- | --- | --- | --- | --- |
| R1-E03-S01 | As an installer, I want a bootstrap admin flow so a fresh deployment can be secured. | First-run admin creation is available once and disabled after setup. | Security integration tests. | Install guide. | R1-E02 |
| R1-E03-S02 | As an enterprise user, I want OIDC login so I can authenticate through corporate identity. | OIDC login, logout, session handling, and group claim mapping work. | Auth integration tests. | Identity setup. | R1-E03-S01 |
| R1-E03-S03 | As an admin, I want tenants and organizations so resources can be scoped. | Tenant and organization CRUD with default tenant for single-org installs. | API and authorization tests. | Admin guide. | R1-E03-S01 |
| R1-E03-S04 | As a security admin, I want roles and permissions so certificate actions are controlled. | Built-in roles exist for admin, operator, requester, approver, auditor, read-only, and service account. | Permission matrix tests. | RBAC guide. | R1-E03-S03 |
| R1-E03-S05 | As an auditor, I want sensitive actions to require reason capture. | Export key, revoke, approve exception, rotate, delete, and break-glass actions capture reason and audit event. | Security tests. | Audit guide. | R1-E03-S04 |
| R1-E03-S06 | As an API integrator, I want service accounts and API tokens so automation can use scoped credentials. | Service accounts can create, rotate, revoke, and scope tokens. | API auth tests. | API auth docs. | R1-E03-S04 |

## Epic R1-E04: Certificate Inventory Core

Goal: Build the system of record for certificates and versions.

| ID | User Story | Acceptance Criteria | Test Scope | Docs | Dependencies |
| --- | --- | --- | --- | --- | --- |
| R1-E04-S01 | As an operator, I want to import a PEM certificate so it appears in inventory. | API accepts PEM certificate and chain, parses metadata, and creates certificate plus version. | Parser and API tests. | Import guide. | R1-E02, R1-E03 |
| R1-E04-S02 | As an operator, I want certificate list search so I can find assets quickly. | Inventory API supports pagination, filtering by owner/status/issuer/expiry/tags, and sorting. | Repository tests. | Inventory docs. | R1-E04-S01 |
| R1-E04-S03 | As an operator, I want a certificate detail view so I can understand status and risk. | Detail API and UI show subject, SANs, issuer, validity, fingerprints, chain, owner, status, and audit timeline. | API and component tests. | Certificate detail docs. | R1-E04-S02 |
| R1-E04-S04 | As a platform engineer, I want certificate deduplication so discoveries from multiple sources merge correctly. | Same fingerprint maps to one version with multiple source observations. | Dedup integration tests. | Discovery model docs. | R1-E04-S01 |
| R1-E04-S05 | As an operator, I want tags and custom metadata so inventory can match local taxonomy. | Tags and typed metadata can be added, searched, audited, and permission-controlled. | API tests. | Metadata docs. | R1-E04-S02 |
| R1-E04-S06 | As an auditor, I want certificate status history so I can reconstruct lifecycle state. | Status transitions produce audit events and are queryable. | Audit tests. | Audit docs. | R1-E04-S03 |

## Epic R1-E05: Frontend Operational Shell

Goal: Provide the UI foundation for a modern operations product.

| ID | User Story | Acceptance Criteria | Test Scope | Docs | Dependencies |
| --- | --- | --- | --- | --- | --- |
| R1-E05-S01 | As a user, I want a role-aware app shell so navigation shows relevant areas. | Shell includes sidebar, top search, user menu, tenant selector, and route guards. | Component and routing tests. | UI guide. | R1-E01-S03, R1-E03 |
| R1-E05-S02 | As an operator, I want an inventory table so I can scan certificates efficiently. | Table supports saved columns, filters, sorting, pagination, empty states, and bulk selection. | Component tests. | Inventory docs. | R1-E04-S02 |
| R1-E05-S03 | As an operator, I want certificate detail pages so I can see lifecycle state in one place. | UI sections include summary, versions, chain, endpoints, destinations, notifications, policy, jobs, and audit placeholders. | Component tests. | Certificate docs. | R1-E04-S03 |
| R1-E05-S04 | As an admin, I want settings pages so tenants, users, groups, and roles can be managed. | UI supports read/write operations according to RBAC. | Workflow tests. | Admin docs. | R1-E03 |
| R1-E05-S05 | As a user, I want global search so I can jump to certificates, endpoints, jobs, and integrations. | Search API and UI return typed results with permission filtering. | API and UI tests. | Search docs. | R1-E04 |

## Epic R2-E01: Certificate Import And Key Handling

Goal: Support real-world certificate onboarding without compromising key safety.

| ID | User Story | Acceptance Criteria | Test Scope | Docs | Dependencies |
| --- | --- | --- | --- | --- | --- |
| R2-E01-S01 | As an operator, I want to import certificate plus private key so legacy assets can be managed. | Import validates key match, stores key through approved secret strategy, and records access policy. | Crypto and security tests. | Key handling docs. | R1-E04, R1-E03 |
| R2-E01-S02 | As an operator, I want bulk import so migrations do not require one certificate at a time. | Bulk import supports dry run, validation report, partial success, and downloadable errors. | Batch tests. | Migration guide. | R2-E01-S01 |
| R2-E01-S03 | As a security admin, I want private key export controls so sensitive key material is protected. | Export is disabled by default, permission-gated, reason-gated, audited, and policy-controlled. | Security tests. | Key export docs. | R2-E01-S01 |
| R2-E01-S04 | As an operator, I want chain validation so imported certificates show trust issues. | Platform detects missing intermediates, invalid chain, expired chain elements, and self-signed status. | Crypto tests. | Troubleshooting docs. | R1-E04-S03 |
| R2-E01-S05 | As an admin, I want external secret references so key material can stay in Vault/KMS. | Certificate version can reference external key or secret location without copying key into DB. | Integration contract tests. | Secrets guide. | R2-E01-S01 |

## Epic R2-E02: Authorities, Issuers, And Profiles

Goal: Model certificate authorities and issuance rules.

| ID | User Story | Acceptance Criteria | Test Scope | Docs | Dependencies |
| --- | --- | --- | --- | --- | --- |
| R2-E02-S01 | As a PKI admin, I want authority records so issuers are managed centrally. | Authority CRUD includes owner, description, connector type, status, roles, and options. | API and permission tests. | Authority docs. | R1-E03 |
| R2-E02-S02 | As a PKI admin, I want certificate profiles so requests use approved defaults. | Profiles define key type, key size, signature algorithm, validity, EKUs, SAN rules, and approval needs. | Validation tests. | Profile docs. | R2-E02-S01 |
| R2-E02-S03 | As an application owner, I want to create a CSR-backed request so I can use an externally generated key. | Request accepts CSR, validates subject/SAN/profile policy, and creates pending request. | API tests. | Request docs. | R2-E02-S02 |
| R2-E02-S04 | As an application owner, I want platform-generated CSR support so I can request certificates without local crypto tooling. | Backend generates key/CSR through approved key strategy and stores references safely. | Crypto tests. | Request docs. | R2-E01-S05 |
| R2-E02-S05 | As a PKI admin, I want a development issuer so local environments can test issuance flows. | Non-production issuer creates test certificates and is clearly blocked from production use by policy. | Integration tests. | Dev setup docs. | R2-E02-S02 |

## Epic R2-E03: ACME And Domain Validation

Goal: Provide standards-based automated issuance.

| ID | User Story | Acceptance Criteria | Test Scope | Docs | Dependencies |
| --- | --- | --- | --- | --- | --- |
| R2-E03-S01 | As a PKI admin, I want an ACME issuer connector so certificates can be issued through ACME CAs. | Connector supports account registration, directory config, EAB where needed, and issuance. | ACME integration tests with test server. | ACME docs. | R2-E02 |
| R2-E03-S02 | As an operator, I want DNS-01 challenge automation so wildcard and internal certificates can be issued. | DNS provider abstraction creates, verifies, and cleans TXT records with retry. | DNS fake provider tests. | DNS validation docs. | R2-E03-S01 |
| R2-E03-S03 | As an operator, I want HTTP-01 challenge support so simple public web certificates can be issued. | Platform can coordinate token placement through destination, agent, or manual workflow. | Integration tests. | HTTP-01 docs. | R2-E03-S01 |
| R2-E03-S04 | As an operator, I want manual validation fallback so restricted environments can still complete issuance. | UI shows required challenge steps, validates completion, and records evidence. | Workflow tests. | Manual validation docs. | R2-E03-S01 |
| R2-E03-S05 | As a PKI admin, I want domain ownership policy so users cannot request arbitrary names. | Requests are checked against approved domain zones, ownership, and environment rules. | Security tests. | Domain policy docs. | R2-E02-S02 |

## Epic R2-E04: Request And Approval Workflows

Goal: Support governed certificate requests and exceptions.

| ID | User Story | Acceptance Criteria | Test Scope | Docs | Dependencies |
| --- | --- | --- | --- | --- | --- |
| R2-E04-S01 | As an application owner, I want a certificate request wizard so I can request compliant certificates. | UI uses profiles and policy to guide subject, SANs, owner, environment, authority, and destinations. | Workflow tests. | Request tutorial. | R2-E02 |
| R2-E04-S02 | As an approver, I want pending request queues so I can review high-risk requests. | Queue filters by owner, profile, domain, risk, and SLA. | API and UI tests. | Approval docs. | R2-E04-S01 |
| R2-E04-S03 | As a security admin, I want approval policies so low-risk requests are automatic and high-risk requests are gated. | Policy can require approval by profile, wildcard, public trust, production, key export, or exception. | Policy tests. | Approval policy docs. | R2-E04-S02 |
| R2-E04-S04 | As an approver, I want approve/reject with comments so decisions are auditable. | Decisions update request state, notify requester, and write audit events. | Workflow tests. | Approval docs. | R2-E04-S02 |
| R2-E04-S05 | As an auditor, I want request evidence so issuance can be traced to approval and policy. | Certificate detail links to request, approval, actor, policy version, and evidence. | Audit tests. | Evidence docs. | R2-E04-S04 |

## Epic R2-E05: Notifications And Escalations

Goal: Prevent outages through timely, routed, and auditable notifications.

| ID | User Story | Acceptance Criteria | Test Scope | Docs | Dependencies |
| --- | --- | --- | --- | --- | --- |
| R2-E05-S01 | As an admin, I want notification channels so messages can be sent to enterprise tools. | Email and webhook channels exist with secret-safe config and test-send. | Integration tests. | Notification setup. | R1-E02 |
| R2-E05-S02 | As an owner, I want expiration notifications so I know when action is required. | Default 60/30/14/7/3/1 day schedules are configurable by policy. | Scheduler tests. | Expiration docs. | R2-E05-S01, R1-E04 |
| R2-E05-S03 | As a security team, I want escalation rules so ignored expiring certs reach the right responders. | Escalation changes recipients by severity, owner response, environment, and time to expiry. | Rule tests. | Escalation docs. | R2-E05-S02 |
| R2-E05-S04 | As an operator, I want notification history so I can prove alerts were sent. | Certificate detail shows notification attempts, targets, channel, status, and error code. | API/UI tests. | Audit docs. | R2-E05-S02 |
| R2-E05-S05 | As a user, I want per-certificate notification settings so routing can be customized. | Settings can add recipients and channels within policy limits. | Permission tests. | Notification docs. | R2-E05-S03 |

## Epic R3-E01: Source Plugins And Discovery

Goal: Discover certificates from existing infrastructure and external systems.

| ID | User Story | Acceptance Criteria | Test Scope | Docs | Dependencies |
| --- | --- | --- | --- | --- | --- |
| R3-E01-S01 | As an admin, I want source connector definitions so discovery integrations can be configured. | Source CRUD includes plugin type, schedule, options, credentials, status, and owner. | API and permission tests. | Source docs. | R1-E02, R2-E01 |
| R3-E01-S02 | As an operator, I want source sync jobs so certificates are imported automatically. | Sync task handles incremental and full sync, deduplication, errors, and metrics. | Worker tests. | Source sync docs. | R3-E01-S01 |
| R3-E01-S03 | As a cloud operator, I want AWS ACM source discovery so cloud certificates are inventoried. | Connector imports ACM certificate metadata across configured accounts and regions. | Contract tests with mocks. | AWS source docs. | R3-E01-S02 |
| R3-E01-S04 | As a Kubernetes operator, I want Kubernetes Secret source discovery so cluster certificates are inventoried. | Connector imports TLS secrets and maps namespace, labels, owner, and cluster. | Integration tests with test cluster or mocks. | Kubernetes source docs. | R3-E01-S02 |
| R3-E01-S05 | As a security engineer, I want CT log discovery so public certificates for owned domains are found. | Source monitors configured domains and imports unknown public certs with risk labels. | Integration/fake feed tests. | CT monitoring docs. | R3-E01-S02 |
| R3-E01-S06 | As an admin, I want source health monitoring so broken discovery does not go unnoticed. | Health state, last sync, freshness, error counts, and alert hooks are visible. | Worker and UI tests. | Source troubleshooting. | R3-E01-S02 |

## Epic R3-E02: Endpoint Scanning And Topology

Goal: Know where certificates are actually deployed.

| ID | User Story | Acceptance Criteria | Test Scope | Docs | Dependencies |
| --- | --- | --- | --- | --- | --- |
| R3-E02-S01 | As an operator, I want TLS endpoint scans so live certificates are observed. | Scanner supports host, port, SNI, timeout, and protocol metadata. | Scanner tests. | Scanner docs. | R1-E04 |
| R3-E02-S02 | As an operator, I want endpoint records so observed runtime locations are tracked. | Endpoint model stores host, port, SNI, protocol, owner, source, observed certificate, and last seen. | API tests. | Endpoint docs. | R3-E02-S01 |
| R3-E02-S03 | As a security engineer, I want endpoint risk detection so expired or weak deployments are surfaced. | Scanner flags expired, untracked, weak, unauthorized, and chain-broken endpoints. | Policy/scanner tests. | Endpoint risk docs. | R3-E02-S02 |
| R3-E02-S04 | As an application owner, I want endpoint-certificate mapping so I can see where a certificate is live. | Certificate detail shows observed endpoints, first seen, last seen, source, and mismatch state. | API/UI tests. | Certificate detail docs. | R3-E02-S02 |
| R3-E02-S05 | As an SRE, I want scan scheduling by CIDR and service inventory so discovery scales. | Scan jobs support CIDR, host list, tags, rate limits, and maintenance windows. | Worker tests. | Scan scheduling docs. | R3-E02-S01 |

## Epic R3-E03: Destination Plugins And Deployment

Goal: Automate certificate deployment to real infrastructure.

| ID | User Story | Acceptance Criteria | Test Scope | Docs | Dependencies |
| --- | --- | --- | --- | --- | --- |
| R3-E03-S01 | As an admin, I want destination connector definitions so deployment targets can be configured. | Destination CRUD includes plugin type, options, credential references, owner, health, and RBAC scope. | API tests. | Destination docs. | R2-E01 |
| R3-E03-S02 | As an operator, I want destination bindings so a certificate knows where it should deploy. | Binding links certificate/profile/request to target account, region, namespace, path, or service. | API/UI tests. | Binding docs. | R3-E03-S01 |
| R3-E03-S03 | As an operator, I want deployment preflight checks so invalid targets fail before rotation. | Preflight validates credentials, target existence, policy, key requirements, and export format. | Plugin contract tests. | Preflight docs. | R3-E03-S02 |
| R3-E03-S04 | As a cloud operator, I want AWS ACM destination support so certificates can deploy to cloud services. | Connector imports/upload certs to ACM and verifies resulting ARN/version metadata. | Mocked integration tests. | AWS destination docs. | R3-E03-S03 |
| R3-E03-S05 | As a Kubernetes operator, I want Kubernetes Secret destination support so certificates deploy to clusters. | Connector writes TLS Secret with owner labels, verifies content, and supports namespace RBAC. | Integration tests. | Kubernetes destination docs. | R3-E03-S03 |
| R3-E03-S06 | As an operator, I want deployment history so I can troubleshoot rotations. | Task detail shows target, status, logs, old/new version, verifier result, retries, and rollback state. | API/UI tests. | Troubleshooting docs. | R3-E03-S03 |

## Epic R3-E04: Automated Renewal And Verified Rotation

Goal: Turn inventory and deployment into outage-preventing automation.

| ID | User Story | Acceptance Criteria | Test Scope | Docs | Dependencies |
| --- | --- | --- | --- | --- | --- |
| R3-E04-S01 | As an owner, I want autorenewal policies so compliant certificates renew without tickets. | Policy defines renewal window, approval needs, retry, and notification behavior. | Policy tests. | Renewal docs. | R2-E02, R2-E05 |
| R3-E04-S02 | As an operator, I want renewal jobs so new certificate versions are issued automatically. | Scheduler creates renewal tasks, prevents duplicates, and handles pending approvals. | Worker tests. | Renewal runbook. | R3-E04-S01 |
| R3-E04-S03 | As an SRE, I want rotation after renewal so certificates deploy to destinations automatically. | New version deploys to bound destinations with preflight, task timeline, and audit events. | End-to-end tests with fake destination. | Rotation docs. | R3-E03, R3-E04-S02 |
| R3-E04-S04 | As an SRE, I want endpoint verification so rotation is confirmed live. | Platform scans expected endpoints and marks rotation verified, partial, or failed. | End-to-end tests. | Verification docs. | R3-E02, R3-E04-S03 |
| R3-E04-S05 | As an operator, I want rollback guidance so failed rotations can be remediated quickly. | Supported destinations can rollback automatically; unsupported ones show runbook and old version details. | Plugin tests. | Rollback docs. | R3-E04-S03 |
| R3-E04-S06 | As a security engineer, I want automation pause controls so risky assets can be protected. | Certificate, owner, profile, destination, or tenant can pause automation with reason and expiry. | Permission tests. | Automation controls docs. | R3-E04-S01 |

## Epic R3-E05: Policy Engine And Risk Scoring

Goal: Make certificate risk measurable and actionable.

| ID | User Story | Acceptance Criteria | Test Scope | Docs | Dependencies |
| --- | --- | --- | --- | --- | --- |
| R3-E05-S01 | As a security admin, I want built-in policy checks so weak certificates are detected. | Checks cover expiration, key size, algorithm, SAN, wildcard, unauthorized CA, owner, notifications, endpoint mismatch, and unmanaged public cert. | Policy tests. | Policy docs. | R1-E04, R3-E02 |
| R3-E05-S02 | As an operator, I want risk scores so the dashboard prioritizes urgent work. | Risk model uses severity, environment, expiry, endpoint exposure, ownership, and policy violations. | Scoring tests. | Risk docs. | R3-E05-S01 |
| R3-E05-S03 | As a security admin, I want policy configuration so standards can differ by tenant and environment. | Policy settings are versioned, audited, permissioned, and previewable. | API/UI tests. | Policy config docs. | R3-E05-S01 |
| R3-E05-S04 | As an owner, I want exception requests so temporary deviations are managed. | Exceptions require reason, expiry, approver, compensating control, and produce reminders. | Workflow tests. | Exception docs. | R2-E04, R3-E05-S03 |
| R3-E05-S05 | As an auditor, I want policy evidence so compliance can be proven. | Evidence export includes policy version, findings, exceptions, approvers, and timestamps. | Export tests. | Evidence docs. | R3-E05-S04 |

## Epic R4-E01: Enterprise Identity And Governance

Goal: Meet enterprise identity, audit, and separation-of-duty expectations.

| ID | User Story | Acceptance Criteria | Test Scope | Docs | Dependencies |
| --- | --- | --- | --- | --- | --- |
| R4-E01-S01 | As an identity admin, I want SAML login so non-OIDC enterprises can integrate. | SAML login, logout, metadata, signing, and group mapping work. | Auth integration tests. | SAML docs. | R1-E03 |
| R4-E01-S02 | As an identity admin, I want SCIM provisioning so users and groups stay synchronized. | SCIM create/update/deactivate user and group mappings are supported. | SCIM contract tests. | SCIM docs. | R1-E03 |
| R4-E01-S03 | As a security admin, I want attribute-based access conditions so permissions can be scoped by environment, owner, and profile. | ABAC conditions combine with RBAC and are tested for deny-by-default behavior. | Security tests. | Access policy docs. | R1-E03 |
| R4-E01-S04 | As an auditor, I want immutable audit export so reviews can use reliable evidence. | Audit events are exportable by time, actor, resource, tenant, and action with integrity metadata. | Audit tests. | Audit export docs. | R1-E02-S04 |
| R4-E01-S05 | As a security lead, I want break-glass access controls so emergencies are traceable. | Break-glass grants expire, require reason, notify security, and create high-severity audit events. | Security tests. | Break-glass docs. | R4-E01-S03 |

## Epic R4-E02: Enterprise Integrations

Goal: Expand connector coverage to common enterprise environments.

| ID | User Story | Acceptance Criteria | Test Scope | Docs | Dependencies |
| --- | --- | --- | --- | --- | --- |
| R4-E02-S01 | As a cloud admin, I want Azure source and destination connectors so Azure certificates are managed. | Azure Key Vault and App Gateway scenarios are supported with mocks and docs. | Connector tests. | Azure docs. | R3-E01, R3-E03 |
| R4-E02-S02 | As a cloud admin, I want GCP source and destination connectors so GCP certificates are managed. | GCP Certificate Manager and load balancer scenarios are supported with mocks and docs. | Connector tests. | GCP docs. | R3-E01, R3-E03 |
| R4-E02-S03 | As a secrets admin, I want Vault/OpenBao source and destination connectors so secret-store certificates are managed. | Connector supports read, write, version, and permission-safe secret paths. | Connector tests. | Vault docs. | R3-E01, R3-E03 |
| R4-E02-S04 | As an ITSM owner, I want ServiceNow and Jira integrations so certificate work can create and update tickets. | Renewal failures, approvals, exceptions, and change references can sync to ITSM. | Integration tests. | ITSM docs. | R2-E04, R2-E05 |
| R4-E02-S05 | As a network admin, I want F5 or appliance connector framework so load balancer rotation is possible. | Framework handles partition/tenant, virtual server mapping, upload, bind, verify, and rollback capability flags. | Contract tests. | Appliance connector docs. | R3-E03 |

## Epic R4-E03: Reporting, Analytics, And Dashboards

Goal: Provide leadership, operator, and audit visibility.

| ID | User Story | Acceptance Criteria | Test Scope | Docs | Dependencies |
| --- | --- | --- | --- | --- | --- |
| R4-E03-S01 | As a security leader, I want an executive dashboard so certificate risk is visible. | Dashboard shows expiring risk, automation coverage, unmanaged certs, violations, ownership gaps, and trends. | API/UI tests. | Dashboard docs. | R3-E05 |
| R4-E03-S02 | As an operator, I want operational dashboards so failed jobs and stale sources are easy to triage. | Dashboard shows queue depth, failed tasks, source freshness, destination failures, and remediation links. | API/UI tests. | Operations docs. | R3-E01, R3-E03 |
| R4-E03-S03 | As an auditor, I want scheduled reports so evidence arrives before reviews. | Reports can be scheduled, scoped, delivered, and audited. | Scheduler tests. | Reporting docs. | R3-E05-S05 |
| R4-E03-S04 | As a data analyst, I want exports so certificate inventory can be analyzed externally. | CSV and JSON exports respect permissions, filters, redaction, and async job limits. | Export tests. | Export docs. | R1-E04 |
| R4-E03-S05 | As a security admin, I want saved views so teams can monitor their slice of risk. | Saved views preserve filters, columns, schedule, and sharing permissions. | API/UI tests. | Saved view docs. | R1-E05-S02 |

## Epic R4-E04: Production Deployment And Operations

Goal: Make the platform easy to deploy, operate, upgrade, and recover.

| ID | User Story | Acceptance Criteria | Test Scope | Docs | Dependencies |
| --- | --- | --- | --- | --- | --- |
| R4-E04-S01 | As an operator, I want Helm charts so Kubernetes deployment is repeatable. | Helm chart configures API, UI, worker, DB references, secrets, ingress, autoscaling, and probes. | Chart lint and install test. | Kubernetes install docs. | R1-E01 |
| R4-E04-S02 | As an operator, I want production configuration validation so bad settings fail fast. | Startup validates required settings, secret references, URL config, auth config, and unsafe defaults. | Config tests. | Config reference. | R1-E02 |
| R4-E04-S03 | As an operator, I want backup and restore docs so data can be recovered. | Backup/restore procedures cover DB, external secrets, plugin config, and audit evidence. | Restore drill checklist. | Backup docs. | R4-E04-S01 |
| R4-E04-S04 | As an SRE, I want OpenTelemetry support so platform behavior is observable. | Metrics, traces, and logs integrate with common collectors. | Observability tests. | Observability docs. | R1-E02-S03 |
| R4-E04-S05 | As a maintainer, I want upgrade procedures so releases can be adopted safely. | Versioned migrations, release notes, compatibility matrix, and rollback notes are maintained. | Upgrade test. | Upgrade docs. | R1-E02-S02 |

## Epic R4-E05: Security Hardening And Compliance

Goal: Harden the platform before broad production adoption.

| ID | User Story | Acceptance Criteria | Test Scope | Docs | Dependencies |
| --- | --- | --- | --- | --- | --- |
| R4-E05-S01 | As a security engineer, I want a threat model so key workflows have known controls. | Threat model covers auth, issuance, key handling, plugin execution, destination deployment, MCP, and admin actions. | Security review. | Security docs. | R3 |
| R4-E05-S02 | As a maintainer, I want dependency and container scanning so supply-chain risk is visible. | CI publishes SBOM and fails on configured severity thresholds. | CI checks. | Security release docs. | R1-E01-S05 |
| R4-E05-S03 | As a security admin, I want rate limits and abuse controls so APIs resist misuse. | API limits apply by actor, token, endpoint, tenant, and sensitive action. | Security tests. | API security docs. | R1-E02 |
| R4-E05-S04 | As a compliance owner, I want retention and purge controls so data lifecycle is governed. | Retention policies cover audit, task logs, scan observations, exports, and deleted resources. | Policy tests. | Retention docs. | R1-E02-S04 |
| R4-E05-S05 | As a security engineer, I want plugin sandbox controls so connector code cannot bypass safeguards. | Plugin permissions, timeouts, classloader/process isolation approach, secret access, and logging redaction are enforced. | Plugin security tests. | Plugin security docs. | R5-E01 |

## Epic R5-E01: Plugin SDK And Marketplace

Goal: Enable safe ecosystem growth without changing core code for every integration.

| ID | User Story | Acceptance Criteria | Test Scope | Docs | Dependencies |
| --- | --- | --- | --- | --- | --- |
| R5-E01-S01 | As a plugin developer, I want a stable plugin API so connectors can be built independently. | Interfaces exist for issuer, source, destination, notification, export, DNS, policy, credential, and ownership plugins. | Contract tests. | Plugin SDK docs. | R3-E01, R3-E03 |
| R5-E01-S02 | As a plugin developer, I want option schemas so plugin config renders safely in UI. | Schema supports validation, secrets, conditionals, examples, and typed UI controls. | Schema tests. | Plugin schema docs. | R5-E01-S01 |
| R5-E01-S03 | As a maintainer, I want plugin test harnesses so connectors can be verified consistently. | Harness provides fake CLM context, secrets, task logging, and contract assertions. | Harness tests. | Plugin testing docs. | R5-E01-S01 |
| R5-E01-S04 | As an admin, I want a plugin catalog so installed integrations are discoverable. | UI shows installed plugins, capabilities, version, health, docs, and compatibility. | API/UI tests. | Integration center docs. | R5-E01-S02 |
| R5-E01-S05 | As a maintainer, I want compatibility checks so upgrades do not silently break plugins. | Plugin metadata declares compatible platform versions and startup rejects incompatible plugins. | Compatibility tests. | Upgrade docs. | R5-E01-S04 |

## Epic R5-E02: Advanced Automation

Goal: Move beyond scheduled renewal into intent-driven certificate operations.

| ID | User Story | Acceptance Criteria | Test Scope | Docs | Dependencies |
| --- | --- | --- | --- | --- | --- |
| R5-E02-S01 | As an operator, I want automation simulations so I can preview upcoming renewals and notifications. | Simulation shows affected certificates, actions, policy gates, risk, and expected dates. | Rule tests. | Simulation docs. | R3-E04 |
| R5-E02-S02 | As an SRE, I want canary rotation so high-risk deployments can roll out gradually. | Destination bindings can define canary order, verification gates, and stop conditions. | End-to-end tests. | Canary docs. | R3-E04 |
| R5-E02-S03 | As an operator, I want owner inference automation so orphaned certificates are assigned faster. | Ownership resolver uses DNS, cloud tags, CMDB, repo metadata, and manual confidence review. | Resolver tests. | Ownership docs. | R3-E01, R3-E02 |
| R5-E02-S04 | As a security engineer, I want remediation campaigns so groups of certificates can be fixed safely. | Campaigns define target set, action plan, approvals, schedule, progress, and rollback notes. | Workflow tests. | Campaign docs. | R3-E05 |
| R5-E02-S05 | As a platform engineer, I want GitOps destination mode so certificate updates can be proposed through pull requests. | Destination creates PRs with generated manifests/bundles and tracks merge/deploy verification. | Integration tests. | GitOps docs. | R3-E03 |

## Epic R5-E03: MCP Server

Goal: Expose policy-aware CLM operations to AI assistants and automation clients.

| ID | User Story | Acceptance Criteria | Test Scope | Docs | Dependencies |
| --- | --- | --- | --- | --- | --- |
| R5-E03-S01 | As an AI platform engineer, I want an MCP server skeleton so CLM tools can be exposed safely. | MCP server runs separately, authenticates to CLM API, and exposes health and capability metadata. | MCP smoke tests. | MCP install docs. | Stable API and R1-E03 |
| R5-E03-S02 | As an operator, I want read-only MCP tools so assistants can answer certificate questions. | Tools include search certificates, get detail, list expiring, list failed jobs, and explain risk. | Tool contract tests. | MCP tool docs. | R5-E03-S01, R3-E05 |
| R5-E03-S03 | As a security admin, I want MCP authorization controls so AI tools cannot bypass RBAC. | MCP calls use scoped service account and enforce tenant, user, and action permissions. | Security tests. | MCP security docs. | R5-E03-S02 |
| R5-E03-S04 | As an operator, I want action MCP tools so approved assistants can create requests and start renewals. | Tools create request, open approval, renew certificate, rotate destination, and generate evidence with reason capture. | Tool integration tests. | MCP action docs. | R5-E03-S03, R2-E04, R3-E04 |
| R5-E03-S05 | As a compliance owner, I want MCP redaction rules so private keys and secrets never appear in AI responses. | MCP layer blocks secret fields and returns safe summaries for sensitive resources. | Security tests. | MCP safety docs. | R5-E03-S03 |

## Epic R5-E04: Developer And Operator Experience

Goal: Make the platform easy to extend, troubleshoot, and trust.

| ID | User Story | Acceptance Criteria | Test Scope | Docs | Dependencies |
| --- | --- | --- | --- | --- | --- |
| R5-E04-S01 | As a developer, I want a CLI so automation can be scripted without raw curl. | CLI supports login, search, import, request, approve, renew, deploy, report, and plugin validation. | CLI tests. | CLI docs. | R1-E02 |
| R5-E04-S02 | As an operator, I want typed error codes so failures map to runbooks. | API, worker, and plugins emit documented codes and remediation hints. | Error tests. | Troubleshooting docs. | R1-E02 |
| R5-E04-S03 | As a support engineer, I want redacted support bundles so issues can be diagnosed safely. | Bundle exports config summary, version, logs, task traces, and selected resource metadata without secrets. | Redaction tests. | Support docs. | R4-E04 |
| R5-E04-S04 | As a docs reader, I want complete tutorials so common workflows can be learned quickly. | Tutorials cover install, import, issue, discover, notify, deploy, rotate, approve, audit, plugin, and MCP. | Docs build. | Docsite tutorials. | DOCSITE.md |
| R5-E04-S05 | As a product owner, I want sample environments so demos and tests are repeatable. | Seed data and demo connectors create realistic certificates, endpoints, risks, and jobs. | Demo smoke tests. | Demo docs. | R3 |

## Cross-Cutting Story Backlog

These stories should be pulled into every relevant epic, not implemented as afterthoughts.

| ID | User Story | Acceptance Criteria | Test Scope | Docs | Dependencies |
| --- | --- | --- | --- | --- | --- |
| CC-S01 | As a user, I want consistent empty, loading, error, and permission-denied states so the UI feels reliable. | Every new page has these states. | Component tests. | UI guidelines. | Any UI story |
| CC-S02 | As a security engineer, I want every sensitive workflow audited so investigations are complete. | Create, update, delete, issue, renew, revoke, export, deploy, approve, exception, auth, and config changes emit audit. | Audit tests. | Audit docs. | Any sensitive story |
| CC-S03 | As an API consumer, I want stable error codes so automation can react programmatically. | New errors include code, message, remediation, correlation ID, and documentation link. | API tests. | Error reference. | Any API story |
| CC-S04 | As an operator, I want all async work observable so failed jobs can be fixed. | New jobs expose status, attempts, timing, logs, metrics, and retry controls. | Worker tests. | Runbooks. | Any worker story |
| CC-S05 | As an admin, I want docs for every integration so connectors can be configured without reading source. | Connector docs include prerequisites, permissions, config, examples, errors, and troubleshooting. | Docs build. | Integration docs. | Any connector story |
| CC-S06 | As a maintainer, I want performance budgets so pages and APIs stay fast as inventory grows. | New list APIs have indexes and pagination; new UI lists virtualize when needed. | Performance tests where relevant. | Performance notes. | Any list story |
| CC-S07 | As a compliance owner, I want tenant isolation verified so data cannot leak. | Tenant-scoped resources have negative authorization tests. | Security tests. | Security docs. | Any tenant story |
| CC-S08 | As a user, I want accessibility support so core workflows are usable by keyboard and screen reader. | New UI flows meet accessibility baseline. | Accessibility tests where feasible. | UI guidelines. | Any UI story |

## Suggested Next Coding Slice

Continue with this sequence:

1. Finish the remaining backend parts of R1-E04-S04 through R1-E04-S06 for source observations, editable metadata/tags, and status history.
2. Implement R1-E05-S01 through R1-E05-S03 for the role-aware shell, inventory table, and certificate detail pages.
3. Move to R2-E01 only after the inventory UI and audit/status model are stable, because private key handling has a much higher security bar.

This keeps the certificate system of record coherent before adding key handling, issuance, destinations, and automation.
