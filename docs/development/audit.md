# Audit Foundation

R1-E02 adds the immutable audit event foundation.

## Audit Event Fields

- `id`
- `occurredAt`
- `actorType`
- `actorId`
- `tenantId`
- `action`
- `resourceType`
- `resourceId`
- `decision`
- `status`
- `reason`
- `correlationId`
- `metadata`

## Current Scope

The service can append audit events and redacts sensitive values from reason and metadata fields.

Identity, tenant enforcement, RBAC decisions, and full sensitive-action coverage start in R1-E03 and later lifecycle epics.

## Redaction

The foundation redacts assignment-style sensitive values such as:

- `password=...`
- `secret=...`
- `token=...`
- `apiKey=...`
- `privateKey=...`
