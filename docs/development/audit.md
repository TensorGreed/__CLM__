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

R1-E03 adds audit coverage for first-run bootstrap admin creation and the sensitive-action reason capture API.

Full certificate lifecycle audit coverage starts when certificate inventory, issuance, renewal, deployment, approval, and key handling workflows are implemented.

## Redaction

The foundation redacts assignment-style sensitive values such as:

- `password=...`
- `secret=...`
- `token=...`
- `apiKey=...`
- `privateKey=...`
