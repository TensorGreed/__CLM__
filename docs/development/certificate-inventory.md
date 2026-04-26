# Certificate Inventory

R1-E04 starts the backend certificate inventory system of record.

## Current Scope

Implemented:

- Public PEM leaf certificate import.
- Optional public PEM chain import.
- Parsed certificate metadata persistence.
- Append-oriented certificate version records.
- SHA-256 fingerprint deduplication per tenant.
- Tenant-scoped list and detail APIs.
- Import audit events.

Not implemented yet:

- Certificate inventory frontend screens.
- Private key import or key storage.
- Issuance, renewal, revocation, or deployment.
- Source observation records from discovery connectors.
- Editable custom metadata fields.
- Full status transition history beyond import audit events.

## Import API

`POST /api/v1/certificates/import` requires `PERMISSION_CERTIFICATE_IMPORT`.

The request accepts:

- `tenantId`
- `certificatePem`
- `chainPem`
- `owner`
- `orphaned`
- `tags`

The API rejects malformed PEM and any payload containing private key PEM blocks. The backend stores the public certificate PEM and parsed public metadata only.

## Parsed Metadata

The parser extracts:

- Subject DN and common name.
- Issuer DN.
- Serial number.
- Validity window.
- Subject alternative names.
- SHA-256 and SHA-1 fingerprints.
- Public key algorithm.
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

## Detail API

`GET /api/v1/certificates/{certificateId}` requires `PERMISSION_CERTIFICATE_READ`.

The response includes:

- Certificate summary fields.
- Current version metadata.
- Version history.
- Imported chain entries for the current version.
- Recent audit timeline entries.

Tenant isolation is enforced before returning the detail response. Resources outside the actor's tenant scope return `RESOURCE_NOT_FOUND`.

## Next Inventory Work

R1-E04 still needs source observation deduplication, editable tags/custom metadata, explicit status transition history, and the frontend inventory table/detail pages in R1-E05.
