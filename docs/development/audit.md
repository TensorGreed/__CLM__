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

R1-E04 adds certificate inventory audit events for PEM imports, duplicate import attempts, source observations, tag updates, metadata updates, and status transitions. Certificate detail responses include the most recent audit events for that certificate.

Full certificate lifecycle audit coverage expands further when certificate metadata edits, status transitions, issuance, renewal, deployment, approval, and key handling workflows are implemented.

## Certificate Inventory Actions

- `certificate.imported`
- `certificate.import_duplicate`
- `certificate.source_observed`
- `certificate.tags_updated`
- `certificate.metadata_updated`
- `certificate.status_changed`

These actions use `resourceType=certificate`, the certificate ID as `resourceId`, the actor and tenant from the authenticated request, and metadata that avoids PEM bodies and private key material.

## Redaction

The foundation redacts assignment-style sensitive values such as:

- `password=...`
- `secret=...`
- `token=...`
- `apiKey=...`
- `privateKey=...`
