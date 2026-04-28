# Certificate Inventory

R1-E04 starts the backend certificate inventory system of record. R2-E01 adds key-handling guardrails, match validation, and external key references without enabling raw private key persistence.

## Current Scope

Implemented:

- Public PEM leaf certificate import.
- Optional public PEM chain import.
- Parsed certificate metadata persistence.
- Append-oriented certificate version records.
- SHA-256 fingerprint deduplication per tenant.
- Tenant-scoped list and detail APIs.
- Source observation records with first-seen, last-seen, and observation counts.
- Editable normalized tags.
- Typed custom metadata.
- Explicit status history.
- Inventory audit events.
- Public key size metadata for RSA, EC, and DSA public keys when derivable from the certificate.
- A guarded private-key import endpoint with RSA/EC key-match validation, audit, redaction, and reference-only storage when explicitly enabled.

Not implemented yet:

- Raw private key persistence, real approved key storage provider integrations, or key export.
- Issuance, renewal, revocation, or deployment.

## Import API

`POST /api/v1/certificates/import` requires `PERMISSION_CERTIFICATE_IMPORT` and is public-certificate-only.

The request accepts:

- `tenantId`
- `certificatePem`
- `chainPem`
- `owner`
- `orphaned`
- `tags`

The API rejects malformed PEM and any payload containing private key PEM blocks. The backend stores the public certificate PEM and parsed public metadata only.

`POST /api/v1/certificates/import-with-private-key` is the guarded legacy key onboarding contract. It validates the public certificate, rejects private keys in certificate and chain fields, verifies that an unencrypted PKCS#8 RSA or EC private key matches the certificate public key, emits audit events, returns `FORBIDDEN` by default, and stores no raw key material.

When `CLM_PRIVATE_KEY_IMPORT_ENABLED=true`, `CLM_KEY_STORAGE_PROVIDER=external-reference`, and `CLM_PRIVATE_KEY_DATABASE_PERSISTENCE_ENABLED=false`, the endpoint stores only an approved `keyReference` for the matching key. Database private key persistence remains rejected.

## Source Observations

`POST /api/v1/certificates/source-observations` requires `PERMISSION_CERTIFICATE_IMPORT`.

The request accepts a public PEM certificate plus source identity fields:

- `tenantId`
- `certificatePem`
- `chainPem`
- `sourceType`
- `sourceKey`
- `observedResourceKey`
- `sourceName`
- `owner`
- `orphaned`
- `tags`
- `sourceMetadata`

The same tenant plus SHA-256 fingerprint maps to one certificate version. Multiple source identities can observe the same version, and repeated observations from the same source update `lastSeenAt` and `observationCount`.

## Parsed Metadata

The parser extracts:

- Subject DN and common name.
- Issuer DN.
- Serial number.
- Validity window.
- Subject alternative names.
- SHA-256 and SHA-1 fingerprints.
- Public key algorithm.
- Public key size in bits when safely derivable from public certificate metadata.
- Signature algorithm.
- Chain length and self-signed flag.
- Public chain entry metadata.

Status is currently derived at import time as `ACTIVE` or `EXPIRED`.

## Search API

`GET /api/v1/certificates` requires `PERMISSION_CERTIFICATE_READ`.

Supported filters:

- `owner`
- `status`
- `issuer`
- `subject`
- `san`
- `tag`
- `metadata.<key>`
- `expiresBefore`
- `expiresAfter`

Supported sorts:

- `createdAt`
- `updatedAt`
- `expiresAt`
- `subject`
- `issuer`
- `owner`
- `status`

Actors without global access are restricted to their assigned tenants even when `tenantId` is omitted.

Global search uses `GET /api/v1/search?q=<term>`. In R1 it returns typed certificate results and applies the same certificate read permission and tenant scope rules.

## Detail API

`GET /api/v1/certificates/{certificateId}` requires `PERMISSION_CERTIFICATE_READ`.

The response includes:

- Certificate summary fields.
- Current version metadata.
- Version history.
- Imported chain entries for the current version.
- Source observations.
- Typed custom metadata.
- External key reference for the current version when one has been validated and recorded.
- Status history.
- Recent audit timeline entries.

Tenant isolation is enforced before returning the detail response. Resources outside the actor's tenant scope return `RESOURCE_NOT_FOUND`.

## Tags And Metadata

`PUT /api/v1/certificates/{certificateId}/tags` requires `PERMISSION_CERTIFICATE_MANAGE`.

Tags are normalized to lowercase and replace the current tag set.

`PUT /api/v1/certificates/{certificateId}/metadata` requires `PERMISSION_CERTIFICATE_MANAGE`.

Metadata values are replaced as a set and support these types:

- `STRING`
- `NUMBER`
- `BOOLEAN`
- `INSTANT`

Metadata keys are normalized to lowercase and keys that describe secrets or credentials are rejected.

## Status History

`POST /api/v1/certificates/{certificateId}/status` requires `PERMISSION_CERTIFICATE_MANAGE`.

The request sets the new certificate status and requires a reason. A changed status creates a `certificate_status_history` row and a `certificate.status_changed` audit event.

`GET /api/v1/certificates/{certificateId}/status-history` requires `PERMISSION_CERTIFICATE_READ`.

## Frontend Inventory UI

R1-E05 adds the first frontend inventory workflow:

- Role-aware operational shell.
- Certificate inventory table.
- Filters, sorting, pagination, saved columns, and bulk selection.
- Certificate detail sections for summary, versions, chain, source observations, metadata, status history, audit events, and automation placeholders.
- Global search for certificate results.

Later inventory work includes real external key storage providers, issuance, renewal, destinations, plugins, MCP, and non-certificate search result types.
