# Product Documentation Site Plan

The docsite is a product surface, not an afterthought. Every feature should be designed with documentation, examples, troubleshooting, and operator adoption in mind.

## Goals

- Help application owners request, renew, deploy, and troubleshoot certificates without needing PKI expertise.
- Help PKI and security teams govern issuance, policy, approvals, exceptions, and audit evidence.
- Help operators deploy, upgrade, monitor, back up, and recover the platform.
- Help developers integrate with APIs, plugins, webhooks, CLI, and future MCP tools.
- Reduce support load through error-code-linked troubleshooting.

## Audiences

- Application owners.
- PKI administrators.
- Security engineers.
- SREs and infrastructure engineers.
- Compliance auditors.
- Plugin developers.
- API and automation developers.
- Platform operators.

## Information Architecture

### Home

- What the platform does.
- Supported lifecycle workflows.
- Quick links by role.
- Current release and support status.

### Getting Started

- Concepts.
- Local install.
- Production install overview.
- First login and bootstrap admin.
- Import your first certificate.
- Request your first certificate.
- Configure your first notification.
- Discover your first endpoint.
- Deploy your first certificate.

### Concepts

- Certificates and versions.
- Private keys and secret references.
- Authorities, issuers, and profiles.
- Sources.
- Endpoints.
- Destinations.
- Requests and approvals.
- Policies, risks, and exceptions.
- Notifications and escalations.
- Plugins.
- Audit events.
- Tenants and RBAC.
- MCP safety model.

### User Guides

- Search inventory.
- Read certificate detail pages.
- Import certificates.
- Request certificates.
- Renew certificates.
- Rotate certificates.
- Revoke certificates.
- Manage owners and tags.
- Configure notifications.
- Request exceptions.
- Review approvals.
- Export evidence.

### Admin Guides

- Tenant setup.
- OIDC setup.
- SAML setup.
- LDAP and group mapping.
- SCIM provisioning.
- Roles and permissions.
- Service accounts and API tokens.
- Authorities and profiles.
- Domain ownership policy.
- Key handling policy.
- Export policy.
- Notification policy.
- Retention policy.

### Operator Guides

- Local development deployment.
- Docker Compose.
- Helm installation.
- Production configuration.
- Database setup.
- Secrets provider setup.
- TLS and ingress.
- Workers and queues.
- Observability.
- Backup and restore.
- Upgrade and rollback.
- Performance tuning.
- High availability.
- Disaster recovery.

### Integration Guides

Each integration page should include:

- Capability type: issuer, source, destination, DNS, notification, export, credential, policy, ownership, metric, or MCP.
- Prerequisites.
- Required permissions.
- Network requirements.
- Secret requirements.
- Configuration fields.
- Example configuration.
- Preflight behavior.
- Supported operations.
- Known limitations.
- Error codes.
- Troubleshooting.
- Security notes.

Initial integration guide families:

- ACME.
- DNS providers.
- AWS.
- Kubernetes.
- Vault and OpenBao.
- Email and webhook notifications.
- Slack and Teams.
- ServiceNow and Jira.
- Azure.
- GCP.
- Appliance connectors.

### API Reference

- Generated OpenAPI reference.
- Authentication.
- Pagination, filtering, and sorting.
- Error model.
- Idempotency.
- Async task model.
- Bulk operations.
- Webhooks.
- Service accounts.
- SDK examples when SDKs exist.

### Plugin Developer Docs

- Plugin architecture.
- Plugin lifecycle.
- Option schema.
- Secret references.
- Permission model.
- Issuer plugin contract.
- Source plugin contract.
- Destination plugin contract.
- Notification plugin contract.
- Export plugin contract.
- DNS plugin contract.
- Policy plugin contract.
- Credential provider contract.
- Test harness.
- Packaging and compatibility.
- Publishing and support.

### MCP Docs

- MCP server architecture.
- Installation.
- Authentication.
- Tool reference.
- Resource reference.
- Prompt reference.
- RBAC and tenant scope.
- Redaction and safety rules.
- Approved use cases.
- Unsafe use cases.
- Audit behavior.
- Troubleshooting.

### Security

- Security model.
- Threat model.
- Private key handling.
- Secret management.
- RBAC and ABAC.
- Audit logging.
- Plugin sandboxing.
- MCP safety.
- Vulnerability reporting.
- Security release process.
- Compliance mapping.

### Troubleshooting

- Certificate import failures.
- Chain validation failures.
- CSR validation failures.
- ACME challenge failures.
- Source sync failures.
- Destination deployment failures.
- Rotation verification failures.
- Notification delivery failures.
- Policy violation explanations.
- Login and identity issues.
- Worker and queue issues.
- Performance issues.

### Release Notes

- New features.
- Breaking changes.
- Security fixes.
- Migration steps.
- Deprecations.
- Plugin compatibility.
- Known issues.

## Page Template: User Workflow

Use this structure for product workflow pages:

```markdown
# Title

One-sentence outcome.

## Who Can Do This

Required roles and permissions.

## Before You Start

Prerequisites, policies, integration config, and known limits.

## Steps

Numbered steps with expected UI labels or API calls.

## Expected Result

What success looks like in the UI/API/audit log.

## Security Notes

Sensitive behavior, audit events, and private key handling.

## Troubleshooting

Common failures, error codes, and remediation.

## Related

Links to concepts, API reference, runbooks, and integration guides.
```

## Page Template: Integration

```markdown
# Integration Name

Supported capabilities and common use cases.

## Capabilities

Issuer, source, destination, DNS, notification, export, or other.

## Prerequisites

Accounts, permissions, network access, and supported versions.

## Permissions

Least-privilege access required.

## Configuration

Field reference with safe examples.

## Validate The Integration

Preflight and test-send/test-sync/test-deploy steps.

## Operations

Supported actions and expected behavior.

## Error Codes

Table of errors and remediation.

## Security Notes

Secret storage, private key handling, and audit events.

## Limitations

Known unsupported scenarios.
```

## Page Template: API

```markdown
# API Area

What this API controls.

## Authentication

Required scopes and roles.

## Common Fields

Resource model summary.

## Endpoints

Link to generated OpenAPI operations.

## Examples

Request and response examples.

## Errors

Stable error codes and remediation.

## Audit Events

Actions recorded in audit log.
```

## Authoring Rules

- Prefer task-oriented pages.
- Keep concepts separate from procedures.
- Include security notes near sensitive steps.
- Include troubleshooting near the workflow that fails.
- Use exact UI labels and API field names once they exist.
- Do not document features before they are implemented unless the page is clearly marked planned.
- Keep examples copy-pasteable once commands exist.
- Link to generated OpenAPI rather than duplicating endpoint details manually.
- Use screenshots only when they clarify workflow, and update them with UI changes.
- Tie error codes to remediation.

## Documentation Definition Of Done

A feature is not done unless:

- User workflow docs are updated.
- Admin/operator docs are updated when configuration or deployment changes.
- API docs are updated when contracts change.
- Plugin docs are updated when connector behavior changes.
- Troubleshooting includes new error codes.
- Security notes cover private key, secret, RBAC, audit, and tenant behavior where relevant.
