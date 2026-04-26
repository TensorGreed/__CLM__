# API Conventions

All public backend APIs live under `/api/v1`.

## Foundation Endpoints

- `GET /api/v1` - API root and links.
- `GET /api/v1/bootstrap/status` - first-run bootstrap status.
- `POST /api/v1/bootstrap/admin` - one-time bootstrap administrator creation.
- `POST /api/v1/certificates/import` - import a public PEM leaf certificate and optional public PEM chain.
- `POST /api/v1/certificates/source-observations` - record a public certificate observation from a source connector or discovery worker.
- `GET /api/v1/certificates` - tenant-scoped certificate inventory list.
- `GET /api/v1/certificates/{certificateId}` - certificate detail with versions, chain, and audit timeline.
- `PUT /api/v1/certificates/{certificateId}/tags` - replace normalized inventory tags.
- `PUT /api/v1/certificates/{certificateId}/metadata` - replace typed custom metadata.
- `POST /api/v1/certificates/{certificateId}/status` - record a status transition with reason.
- `GET /api/v1/certificates/{certificateId}/status-history` - read status transition history.
- `GET /api/v1/tasks/{taskId}` - redacted async task status requiring `PERMISSION_TASK_READ`.
- `GET /v3/api-docs` - generated OpenAPI JSON.
- `GET /swagger-ui` - generated Swagger UI.

Certificate inventory APIs do not accept private keys in R1-E04. Key import, issuance, renewal, destinations, plugins, and MCP are later epics.

## Authentication

The API supports HTTP Basic for local users, bearer tokens for service accounts, and optional OIDC login. Protected endpoints return `UNAUTHENTICATED` when credentials are missing or invalid, and `FORBIDDEN` when the actor lacks the required permission.

Service account bearer tokens are tenant-scoped and permission-scoped. Raw token values are returned only at creation or rotation time.

Certificate inventory endpoints use:

- `PERMISSION_CERTIFICATE_READ` for list, detail, and status-history reads.
- `PERMISSION_CERTIFICATE_IMPORT` for PEM import and source observations.
- `PERMISSION_CERTIFICATE_MANAGE` for tags, metadata, and status updates.

## Error Envelope

Errors use a stable JSON envelope:

```json
{
  "code": "RESOURCE_NOT_FOUND",
  "message": "Task '...' was not found.",
  "remediation": "Verify the resource identifier and tenant scope.",
  "correlationId": "request-correlation-id",
  "path": "/api/v1/tasks/...",
  "timestamp": "2026-04-26T00:00:00Z",
  "details": []
}
```

The `code` field is intended for automation. The `message` and `remediation` fields are safe for users and must not contain secrets.

## Correlation IDs

Clients may send `X-Correlation-ID`.

If the header is missing or invalid, the API generates a UUID. The resolved value is returned in the response header and written to logs through MDC as `correlationId`.

## Query Primitives

List APIs use shared query primitives:

- Page numbers are zero-based.
- Default page size is `25`.
- Maximum page size is `100`.
- Sort syntax is `field,asc` or `field,desc`.
- Filter syntax is `field:value`.

`GET /api/v1/certificates` supports:

- `tenantId`: optional tenant filter; non-global actors are still restricted to assigned tenants.
- `filter=owner:value`
- `filter=status:ACTIVE|EXPIRED|REVOKED`
- `filter=issuer:value`
- `filter=subject:value`
- `filter=san:value`
- `filter=tag:value`
- `filter=metadata.environment:prod`
- `filter=expiresBefore:2026-12-31T00:00:00Z`
- `filter=expiresAfter:2026-01-01T00:00:00Z`
- `sort=createdAt,desc`
- `sort=updatedAt,desc`
- `sort=expiresAt,asc`
- `sort=subject,asc`
- `sort=issuer,asc`
- `sort=owner,asc`
- `sort=status,asc`

Responses use the shared page envelope with `content`, page metadata, and total counts.

## OpenAPI

OpenAPI is generated from the Spring MVC application.

Use:

```bash
curl http://localhost:8080/v3/api-docs
```
