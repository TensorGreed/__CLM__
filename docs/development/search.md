# Search

R1-E05 adds global search for certificate inventory.

## API

`GET /api/v1/search` requires `PERMISSION_CERTIFICATE_READ`.

Query parameters:

- `q`: search term, up to 200 characters.
- `tenantId`: optional tenant scope.
- `limit`: optional result limit from 1 to 25.

The response returns typed results with:

- `type`
- `id`
- `tenantId`
- `title`
- `subtitle`
- `status`
- `href`
- `matchedFields`

R1 search returns certificate results first. It searches common name, subject, issuer, serial number, SHA-256 fingerprint, SANs, and owner. Certificate read permission and tenant scope are enforced by the inventory service.

## Frontend

The app shell top search routes to `/search?q=<term>`.

Search results link to certificate detail pages. Empty, loading, error, and permission-denied states use the same route-state patterns as inventory.

## Later Work

Endpoint, job, integration, destination, source, and policy result types are added in later epics when those resources exist.
