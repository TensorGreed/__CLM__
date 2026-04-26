# API Conventions

All public backend APIs live under `/api/v1`.

## Foundation Endpoints

- `GET /api/v1` - API root and links.
- `GET /api/v1/bootstrap/status` - first-run bootstrap status.
- `POST /api/v1/bootstrap/admin` - one-time bootstrap administrator creation.
- `GET /api/v1/tasks/{taskId}` - redacted async task status requiring `PERMISSION_TASK_READ`.
- `GET /v3/api-docs` - generated OpenAPI JSON.
- `GET /swagger-ui` - generated Swagger UI.

No certificate lifecycle business endpoints exist yet.

## Authentication

The API supports HTTP Basic for local users, bearer tokens for service accounts, and optional OIDC login. Protected endpoints return `UNAUTHENTICATED` when credentials are missing or invalid, and `FORBIDDEN` when the actor lacks the required permission.

Service account bearer tokens are tenant-scoped and permission-scoped. Raw token values are returned only at creation or rotation time.

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

R1-E02 adds shared primitives for future list APIs:

- Page numbers are zero-based.
- Default page size is `25`.
- Maximum page size is `100`.
- Sort syntax is `field,asc` or `field,desc`.
- Filter syntax is `field:value`.

These primitives are not attached to certificate inventory yet.

## OpenAPI

OpenAPI is generated from the Spring MVC application.

Use:

```bash
curl http://localhost:8080/v3/api-docs
```
