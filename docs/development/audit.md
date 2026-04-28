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

R2-E01 adds audit coverage for private-key match validation, validation failures, reference-only key association, and authorized private-key import attempts rejected by policy.

Full certificate lifecycle audit coverage expands further when issuance, renewal, deployment, approval, export, and real key storage workflows are implemented.

## Certificate Inventory Actions

- `certificate.imported`
- `certificate.import_duplicate`
- `certificate.source_observed`
- `certificate.tags_updated`
- `certificate.metadata_updated`
- `certificate.status_changed`
- `certificate.private_key_match_validated`
- `certificate.private_key_match_failed`
- `certificate.key_reference_recorded`
- `certificate.private_key_import_rejected`

Certificate inventory actions use the actor and tenant from the authenticated request and metadata that avoids PEM bodies and private key material. Persisted certificate actions use `resourceType=certificate` and the certificate ID as `resourceId`. Private-key validation attempts that happen before a certificate is persisted use `resourceType=certificate_private_key_import` and the tenant ID as `resourceId`.

## Redaction

The foundation redacts assignment-style sensitive values such as:

- `password=...`
- `secret=...`
- `token=...`
- `apiKey=...`
- `privateKey=...`

It also redacts full private key PEM blocks before diagnostic text is returned or persisted.
