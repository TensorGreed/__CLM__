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
- Springdoc OpenAPI.
- Logstash Logback Encoder for JSON logs.

## Current Security Behavior

Only these endpoints are public:

- `/actuator/health`
- `/actuator/health/**`
- `/actuator/info`
- `/api/v1`
- `/api/v1/tasks/{taskId}`
- `/v3/api-docs/**`
- `/swagger-ui/**`

All other endpoints are denied until the identity and RBAC stories are implemented. The public foundation endpoints return only non-secret metadata and redacted task state.

## Test Profile

The `test` profile uses H2 in PostgreSQL compatibility mode so context and security tests can run without a local database.

## Configuration

Local runtime configuration uses:

- `CLM_DATABASE_URL`
- `CLM_DATABASE_USERNAME`
- `CLM_DATABASE_PASSWORD`
- `PORT`

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
