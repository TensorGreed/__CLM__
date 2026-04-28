# Backend Foundation

The backend is a Spring Boot 3.5 application running on Java 21.

## Included Starters

- Spring Web.
- Spring Boot Actuator.
- Spring Data JPA.
- Flyway.
- PostgreSQL driver.
- Validation.
- Spring Security.
- Spring OAuth2 Client for OIDC login.
- Springdoc OpenAPI.
- Logstash Logback Encoder for JSON logs.

## Current Security Behavior

Only these endpoints are public:

- `/actuator/health`
- `/actuator/health/**`
- `/actuator/info`
- `/api/v1`
- `/api/v1/bootstrap/status`
- `POST /api/v1/bootstrap/admin`
- `/v3/api-docs/**`
- `/swagger-ui/**`

All other endpoints require authentication. Local administrators can use HTTP Basic after the first-run bootstrap flow creates the initial admin account. Service accounts authenticate with bearer tokens. OIDC login is available when `CLM_OIDC_ENABLED=true` and Spring OAuth client registration settings are supplied.

Protected controller methods use `@PreAuthorize` with permission authorities such as `PERMISSION_TENANT_READ`. Security denials return the stable API error envelope with `UNAUTHENTICATED` or `FORBIDDEN`.

## Test Profile

The `test` profile uses H2 in PostgreSQL compatibility mode so context and security tests can run without a local database.

## Configuration

Local runtime configuration uses:

- `CLM_DATABASE_URL`
- `CLM_DATABASE_USERNAME`
- `CLM_DATABASE_PASSWORD`
- `PORT`
- `CLM_OIDC_ENABLED`
- `CLM_OIDC_GROUP_CLAIM`
- `CLM_PRIVATE_KEY_IMPORT_ENABLED`
- `CLM_KEY_STORAGE_PROVIDER`
- `CLM_PRIVATE_KEY_DATABASE_PERSISTENCE_ENABLED`

The default database values match `docker-compose.yml`.

## Implemented Platform Foundations

- Versioned API root at `/api/v1`.
- OpenAPI JSON at `/v3/api-docs`.
- Stable error envelope for API exceptions.
- Query primitives for pagination, sorting, and filtering.
- Flyway baseline migration for audit events and task runs.
- JSON logs with request correlation IDs.
- Audit event service.
- Async task run service and task runner abstraction.
- First-run bootstrap admin flow.
- Tenant and organization management APIs.
- Built-in roles and permission authorities.
- Service account and API token authentication.
- Sensitive-action reason capture with audit events.
- Certificate inventory import, search, and detail APIs.
- Public PEM certificate parsing with private-key rejection.
- Disabled private-key import guardrail path with audit and redaction.
- Public key size metadata extraction for certificate versions.
- Certificate version fingerprint deduplication.
- Certificate source observation records.
- Certificate tag, typed metadata, and status history APIs.
