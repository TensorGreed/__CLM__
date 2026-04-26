# CLM Platform Features

## Product Intent

Build a production-grade certificate lifecycle management platform inspired by Netflix Lemur, but modernized for Spring Boot, React, cloud-native deployment, enterprise governance, and future MCP integration.

The platform should become the system of record and automation plane for certificates, keys, authorities, endpoints, owners, deployment targets, policy compliance, and lifecycle events across hybrid infrastructure.

## Reference Baseline

Use these references as product context, not as code to copy:

- [Netflix Lemur repository](https://github.com/Netflix/lemur)
- [Lemur documentation](https://lemur.readthedocs.io/)
- [Lemur plugin structure](https://lemur.readthedocs.io/en/v1.7.0/developer/plugins/)
- [Lemur administration and plugins](https://lemur.readthedocs.io/en/latest/administration.html)
- [DigiCert Trust Lifecycle Manager feature themes](https://www.digicert.com/trust-lifecycle-manager)
- [Keyfactor Command feature themes](https://www.keyfactor.com/products/command/)
- [Smallstep Certificate Manager feature themes](https://smallstep.com/certificate-manager/how-it-works/)

Core Lemur parity concepts to keep: certificates, authorities, users, roles, notifications, destinations, sources, endpoints, exports, logs, plugin extension points, and scheduled automation.

## Product Principles

- Certificate operations must be safe by default, automated where practical, and auditable everywhere.
- The platform must be CA-agnostic and infrastructure-agnostic.
- Manual workflows must remain available, but every repeatable manual step should have an API and automation path.
- Every certificate must have an owner, risk posture, source of truth, lifecycle policy, and observable deployment state.
- Private keys must be protected by design. Prefer non-exportable keys, HSM/KMS/Vault references, and just-in-time access over storing raw key material.
- The UI must be dense, modern, searchable, and operationally useful. Avoid marketing layouts inside the app.
- Integrations must be built through stable contracts so core product velocity is not blocked by connector churn.
- Documentation is a first-class product surface. Every feature should ship with operator, developer, API, and troubleshooting documentation.
- MCP should expose safe, policy-aware operations rather than unrestricted database or secret access.

## Primary Personas

- Platform security engineer: owns certificate policy, CA integrations, exception management, and audit posture.
- PKI administrator: manages authorities, issuance profiles, revocation, root/intermediate trust, and compliance.
- Application owner: requests, renews, deploys, and tracks certificates for owned services.
- SRE or infrastructure engineer: automates certificate deployment to load balancers, Kubernetes, gateways, hosts, and secrets stores.
- Compliance auditor: reviews certificate inventory, controls, evidence, approvals, and historical lifecycle events.
- Developer platform engineer: integrates CLM APIs, plugins, Terraform, CI/CD, and future MCP tools into workflows.
- Support engineer: diagnoses failed issuance, failed deployment, expired certificate incidents, and endpoint mismatches.

## Target Architecture

### Backend

- Java 21 LTS or project-approved newer LTS.
- Spring Boot 3.x with modular package boundaries.
- Spring Security with OIDC/SAML integration and service-account authentication.
- PostgreSQL as primary transactional store.
- Flyway for schema migration.
- Async workers for scans, renewals, deployments, notification sends, policy evaluation, and plugin execution.
- Queue abstraction that can run locally first and later back onto Kafka, RabbitMQ, SQS, or cloud queues.
- OpenAPI-first REST API with versioned public contracts.
- Event model for audit, notifications, automation triggers, and external integrations.
- Plugin API isolated from core domain services.

### Frontend

- React with TypeScript.
- Modern app shell with responsive operational views.
- Data-fetching layer with request caching and optimistic UI where safe.
- Role-aware navigation and actions.
- Dashboard, inventory tables, detail pages, task timelines, topology views, policy views, and integration consoles.
- Accessibility baseline: keyboard navigation, semantic controls, focus states, color contrast, reduced-motion support.

### Deployment

- Docker images for API, worker, UI, and optional MCP server.
- Helm chart and Kubernetes deployment docs.
- Compose profile for local development.
- Externalized configuration through environment variables and secrets providers.
- Production reference deployments for single-node, high-availability, and multi-region read-heavy modes.

### Future MCP

- Separate MCP server process that calls the public API with scoped service credentials.
- Tool names aligned to product operations: search certificates, explain risk, create request, renew certificate, rotate destination, list failed automations, generate evidence.
- MCP tools must respect RBAC, tenant scope, approval rules, and private-key export policy.

## Domain Model

The domain model should be implemented explicitly and documented continuously.

- Tenant: enterprise isolation boundary for SaaS or internal multi-organization deployment.
- Organization: business unit, department, or managed customer inside a tenant.
- Environment: production, staging, development, lab, or custom deployment context.
- User: human actor authenticated through local bootstrap, OIDC, SAML, LDAP, or SCIM.
- Service account: non-human actor with scoped API permissions and credential rotation.
- Role: named permission bundle.
- Group: identity-provider or local group mapped to roles and ownership.
- Permission: fine-grained action on resource type and scope.
- Certificate: logical certificate identity tracked across versions.
- Certificate version: a specific issued certificate with serial, SANs, validity, chain, and fingerprints.
- Private key reference: pointer to KMS, HSM, Vault, Kubernetes Secret, or encrypted storage metadata.
- CSR: certificate signing request and provenance.
- Authority: CA or issuing authority configuration.
- Issuer connector: plugin-backed integration that issues, renews, revokes, or validates certificates.
- Certificate profile: allowed key types, algorithms, validity, EKUs, name constraints, templates, and approval rules.
- Source: discovery integration that imports certificates or endpoint observations.
- Destination: deployment integration that installs certificates or key material.
- Endpoint: observed or declared runtime location serving or consuming a certificate.
- Deployment binding: relationship between a certificate and destination-specific target.
- Automation policy: rule set that controls discovery, renewal, rotation, notifications, and remediation.
- Notification: channel, recipient, cadence, template, and escalation policy.
- Workflow: approval, review, exception, or remediation process.
- Task run: async execution instance with inputs, outputs, status, logs, retries, and actor.
- Plugin: installable extension with metadata, option schema, permissions, and execution contract.
- Credential: secret reference used by connectors, never exposed directly through UI or logs.
- Audit event: immutable record of actor, action, resource, decision, and outcome.
- Evidence bundle: exportable compliance package for an asset, control, incident, or audit period.
- Policy violation: detected misconfiguration, expiration risk, weak crypto, unauthorized issuer, or ownership gap.
- Exception: time-boxed approval allowing a policy violation with compensating controls.

## Feature Catalog

### Certificate Inventory

- Searchable inventory of all certificates, certificate versions, chains, fingerprints, and metadata.
- Parse PEM, DER, PKCS#7, PKCS#12, JKS, and truststore formats where appropriate.
- Track CN, SANs, issuer, subject, serial number, SKI, AKI, key algorithm, key size, signature algorithm, EKUs, validity, chain, status, owner, tags, source, destinations, endpoints, and risk score.
- Deduplicate certificates discovered through multiple sources.
- Track unknown, imported, managed, retired, revoked, expired, abandoned, and externally managed states.
- Support custom metadata fields and typed tags.
- Show chain completeness, trust path, root/intermediate details, and chain reuse.
- Show certificate lineage across renewals and reissues.

### Issuance

- Self-service certificate request flow.
- Authority selection based on policy, tenant, environment, DNS name, certificate profile, owner, and destination.
- CSR upload or generated CSR.
- Server TLS, client TLS, mTLS, device, user, code signing, S/MIME, SSH certificate, and custom profile support as later maturity goals.
- Approval workflows based on risk and policy.
- ACME issuance with HTTP-01, DNS-01, and TLS-ALPN-01 where feasible.
- Public CA integrations and private CA integrations.
- Reissue, duplicate, renew, revoke, suspend, resume, and retire flows.
- Certificate import with optional private key and chain.
- Bulk import and migration tooling.

### Renewal And Rotation

- Renewal windows by profile, owner, environment, or certificate.
- Automatic renewal with policy and approval gates.
- Automatic deployment to destinations after issuance.
- Endpoint-aware rotation that verifies the new certificate is live.
- Rollback strategy for failed deployment where supported by destination.
- Staged rollout, canary deployment, and maintenance-window support.
- Renewal dry run and preflight checks.
- Escalation when automated renewal is blocked.
- Disable or pause autorotation under policy conditions.

### Discovery

- Source plugins for CA inventory, cloud inventory, secret stores, Kubernetes, network TLS scans, CT logs, CMDBs, and file-system agents.
- Active network scans by host, CIDR, port, SNI, and protocol.
- Passive discovery through logs, events, and cloud APIs where possible.
- CT log monitoring for public certificates issued for owned domains.
- Ownership inference from DNS, cloud tags, CMDB, Kubernetes labels, repository metadata, and identity provider groups.
- Discovery scheduling, retry, rate limiting, incremental sync, and full resync.
- Source trust levels and conflict resolution.

### Endpoints

- Track runtime locations where certificates are observed or expected.
- Endpoint fields: host, port, SNI, protocol, registry type, source, owner, environment, aliases, certificate path, observed chain, policy, cipher posture, and last seen.
- Detect endpoint-certificate mismatches.
- Detect stale endpoints still serving old certificates after rotation.
- Detect endpoints serving untracked, expired, weak, or unauthorized certificates.
- Support endpoint groups and service topology.
- Support endpoint evidence for audits and outage postmortems.

### Destinations

- Destination plugins deploy certificates to infrastructure.
- Required destination contract: validate options, preflight, upload, activate, verify, rollback where supported, and clean old versions.
- Destination bindings define target account, region, namespace, secret path, load balancer, gateway, service, file path, or application-specific location.
- Support private-key-required and certificate-only destinations.
- Support export transformers before deployment.
- Support maintenance windows, change ticket references, approvals, and retry policies.

### Export Formats

- PEM certificate, key, and chain bundles.
- PKCS#12/PFX with configurable alias and encryption.
- JKS and truststore output.
- DER and PKCS#7 where useful.
- Kubernetes TLS Secret manifest.
- NGINX, Apache, HAProxy, Caddy, Tomcat, Java, IIS, Envoy, and generic bundle profiles.
- Redacted exports for documentation and support.
- Export policy checks, watermarking, reason capture, and audit events.

### Plugins

- Plugin types: issuer, source, destination, notification, export, DNS challenge, policy evaluator, ownership resolver, credential provider, membership provider, metric sink, webhook, and MCP tool provider.
- Plugin metadata: id, title, version, type, vendor, description, capabilities, option schema, permission needs, secret needs, docs URL, support status, and compatibility range.
- Plugin option schema should support string, number, integer, boolean, select, multiselect, secret reference, JSON object, array, map, regex validation, conditional fields, defaults, help text, and examples.
- Plugin runtime should isolate failures, redact logs, enforce timeouts, and expose typed error codes.
- Plugin SDK should include test harnesses, sample plugins, documentation, and compatibility checks.
- Marketplace/catalog view for installed and available plugins.

### Policy And Compliance

- Policy engine for cryptographic standards, naming rules, validity limits, allowed CAs, approved profiles, ownership, destinations, renewal windows, export constraints, and exception handling.
- Policy-as-code support later through YAML or Rego-style rules if justified.
- Built-in checks: weak key, SHA-1, MD5, too-long validity, missing SAN, wildcard risk, unauthorized CA, expired intermediate, missing owner, missing notifications, missing endpoints, unmanaged public certificate, private key export risk, stale deployment, noncompliant EKU.
- Risk scoring per certificate, endpoint, owner, environment, tenant, and business service.
- Evidence exports for auditors.
- Exception workflow with owner, approver, expiry, reason, compensating control, and review reminders.

### Identity And Access

- Bootstrap local admin for first install only.
- OIDC login.
- SAML login.
- LDAP group mapping.
- SCIM user and group provisioning.
- Tenant, organization, project, and resource-level RBAC.
- Attribute-based conditions for environment, owner, certificate profile, CA, and action.
- Just-in-time access for sensitive actions.
- Separate permissions for viewing public metadata, viewing private key availability, exporting private key, requesting, approving, revoking, rotating, and managing connectors.
- Break-glass role with mandatory reason, expiry, and high-severity audit event.

### Notifications

- Channels: email, Slack, Microsoft Teams, PagerDuty, Opsgenie, ServiceNow, Jira, webhook, AWS SNS, and generic event bus.
- Notification types: expiration, renewal due, renewal success, renewal failure, deployment success, deployment failure, validation failure, revocation, policy violation, exception expiring, source sync failure, destination drift, security summary, owner missing, CT discovery, and incident.
- Cadence and escalation policies.
- Owner, team, security, service desk, and custom recipient targeting.
- Templates with safe variables and localization-ready structure.
- Per-certificate notification controls with policy guardrails.

### Automation

- Scheduler for recurring jobs.
- Workflow engine for long-running issuance, renewal, deployment, scan, and approval jobs.
- Idempotent task execution.
- Locking by resource and destination binding.
- Retry with backoff, circuit breaker, and dead-letter state.
- Preflight checks before risky operations.
- Automation rules based on certificate attributes, endpoints, tags, owner, risk, and environment.
- Simulation mode to preview upcoming automations and notifications.

### Observability

- Structured JSON logs with trace and correlation IDs.
- Metrics for API latency, worker latency, queue depth, source sync duration, destination deployment duration, renewal success rate, policy violations, plugin errors, notification sends, and inventory freshness.
- OpenTelemetry traces for user actions and worker jobs.
- Health checks, readiness checks, and dependency checks.
- Admin dashboard for automation backlog and failed jobs.
- Support bundles with redaction.

### Reporting And Analytics

- Expiration calendar and trend chart.
- Inventory by owner, CA, source, environment, risk, key algorithm, validity, and status.
- Automation coverage report.
- Certificates without owners, endpoints, notifications, policy, or automation.
- Public certificate discovery report.
- Failed renewal and failed deployment reports.
- Audit evidence report.
- Compliance benchmark report.
- CSV, JSON, and API export.

### API And CLI

- Versioned REST API.
- OpenAPI docs generated from backend contracts.
- Stable error model with machine-readable codes.
- Pagination, filtering, sorting, and sparse fieldsets.
- Bulk APIs for imports, ownership changes, tagging, and automation actions.
- Webhook/event subscriptions.
- CLI for operators and CI/CD automation.
- API token and service account management.

### MCP Server

- MCP server should be added after stable API, auth, and policy controls exist.
- Initial tools: search certificates, get certificate detail, explain certificate risk, list expiring certificates, list failed jobs, create certificate request, renew certificate, rotate destination, open approval, generate evidence bundle.
- MCP resources: certificate summaries, policy summaries, integration catalog, audit snippets, runbook references.
- MCP prompts: outage triage, renewal campaign planning, migration assessment, plugin scaffolding, evidence generation.
- MCP responses must avoid returning private keys or secrets.

### UI Experience

- First screen after login: operational dashboard with expiring risk, failed automation, unmanaged certificates, policy violations, and inventory freshness.
- Global search across certificates, endpoints, owners, authorities, jobs, and integrations.
- Inventory table with saved views and bulk actions.
- Certificate detail page with summary, versions, chain, endpoints, destinations, automations, notifications, audit events, policy, and owner history.
- Request wizard with policy-aware defaults.
- Renewal and rotation timeline.
- Integration center with source, destination, issuer, notification, export, and DNS plugins.
- Endpoint topology and scan result views.
- Policy center with violations, exceptions, and rule configuration.
- Task run detail with logs, retries, and remediation.
- Admin console for tenants, users, groups, roles, settings, secrets, and system health.
- Documentation and support links from every complex workflow.

### Documentation Site

- Product docs, admin docs, operator runbooks, developer docs, plugin SDK docs, API reference, MCP docs, tutorials, and architecture docs.
- Every epic in ROADMAP.md should include docsite impact.
- Docs should use runnable examples once code exists.
- Troubleshooting pages should be tied to typed error codes.

## Integration Backlog

### Issuers And CAs

- ACME v2: Let's Encrypt, ZeroSSL, internal ACME, DigiCert ACME, Sectigo ACME.
- Public CAs: DigiCert, Sectigo, Entrust, GlobalSign.
- Private CAs: HashiCorp Vault PKI, OpenBao, Smallstep step-ca, CFSSL, Microsoft AD CS, AWS Private CA, Google CAS, Azure Key Vault certificates.
- Enterprise CLM bridges: Venafi, Keyfactor, DigiCert Trust Lifecycle Manager where customers need coexistence.
- Custom REST issuer and script-backed issuer for controlled enterprise use.

### DNS And Domain Validation

- AWS Route 53.
- Cloudflare.
- Azure DNS.
- Google Cloud DNS.
- Akamai Edge DNS.
- NS1.
- Infoblox.
- RFC 2136.
- Custom webhook DNS provider.
- Manual DNS approval workflow.

### Sources

- TLS network scanner.
- Certificate Transparency logs.
- AWS ACM, IAM Server Certificates, ELB, ALB/NLB listeners, CloudFront, API Gateway.
- Azure Key Vault, App Gateway, Front Door, App Service.
- GCP Certificate Manager, HTTPS load balancers, Secret Manager.
- Kubernetes Secrets, Ingress, Gateway API, cert-manager objects.
- HashiCorp Vault and OpenBao.
- Filesystem or host agent.
- NGINX, Apache, HAProxy, Caddy, Envoy, Tomcat, IIS.
- F5 BIG-IP, Citrix ADC, Palo Alto, Fortinet, and other network appliances as later connector families.
- ServiceNow CMDB.
- GitHub, GitLab, Bitbucket repository scanning for committed certificates.

### Destinations

- AWS ACM, IAM, ELB/ALB/NLB, CloudFront, API Gateway.
- Azure Key Vault, App Gateway, Front Door, App Service.
- GCP Certificate Manager, load balancers, Secret Manager.
- Kubernetes TLS Secrets, cert-manager Certificate resources, Gateway API.
- HashiCorp Vault and OpenBao paths.
- NGINX, Apache, HAProxy, Caddy, Envoy, Tomcat, Jetty, IIS.
- F5 BIG-IP and enterprise appliances.
- SFTP, SCP, SMB, and file-drop destinations.
- Java JKS/P12 deployments.
- Webhook destination.
- Terraform Cloud or GitOps pull request destination as later safe deployment modes.

### Notifications And ITSM

- SMTP and SES.
- Slack.
- Microsoft Teams.
- PagerDuty.
- Opsgenie.
- ServiceNow.
- Jira.
- AWS SNS.
- Generic webhook.
- Event bus: Kafka, RabbitMQ, AWS EventBridge.

### Secrets And Credentials

- HashiCorp Vault.
- OpenBao.
- AWS Secrets Manager and KMS.
- Azure Key Vault.
- GCP Secret Manager and Cloud KMS.
- Kubernetes Secrets for dev and limited deployment scenarios.
- HSM integration through PKCS#11 or cloud HSM services as later maturity.

## Nonfunctional Requirements

### Security

- No secret or private key in logs.
- Private key export disabled by default.
- All sensitive actions require authorization, reason capture, and audit.
- Support encryption at rest through database, application field encryption, and external KMS where appropriate.
- CSRF, CORS, session, token, and cookie controls must be explicit.
- Secure defaults for TLS, headers, rate limits, and login.
- SBOM, dependency scanning, SAST, container scanning, and secret scanning in CI.
- Threat model must be maintained for key workflows.

### Performance

- Inventory search must scale to millions of certificate versions and endpoint observations.
- API list endpoints must be paginated and indexed.
- Long operations must run asynchronously.
- Scans and plugin runs must be rate-limited and shardable.
- UI pages must load useful above-the-fold information quickly and progressively load heavy details.

### Reliability

- Idempotent issuance, renewal, deployment, and notification operations.
- Retry and resumability for long-running jobs.
- Transactional audit records for state transitions.
- Backup and restore procedures.
- No single worker should be able to corrupt global lifecycle state.

### Operability

- Health and readiness endpoints.
- Admin system dashboard.
- Redacted support bundles.
- Runbooks for common failures.
- Config validation at startup.
- Upgrade and rollback documentation.

### Compliance

- Immutable audit log strategy.
- Evidence exports.
- Separation of duties for request and approval.
- Policy exceptions with expiry.
- Data retention and purge controls.
- Tenant isolation testing.

## Maturity Levels

- Level 0: Documentation, architecture, and repo standards.
- Level 1: Core inventory, auth, manual import, and UI read model.
- Level 2: Issuance, notifications, sources, destinations, and scheduled jobs.
- Level 3: Automated renewal and endpoint-verified rotation.
- Level 4: Enterprise policy, approvals, SCIM, audit evidence, and high availability.
- Level 5: Plugin SDK, marketplace, MCP server, advanced analytics, and broad connector ecosystem.
