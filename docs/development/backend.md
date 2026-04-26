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

## Current Security Behavior

Only these endpoints are public:

- `/actuator/health`
- `/actuator/health/**`
- `/actuator/info`

All other endpoints are denied until the identity and RBAC stories are implemented.

## Test Profile

The `test` profile uses H2 in PostgreSQL compatibility mode so context and security tests can run without a local database.

## Configuration

Local runtime configuration uses:

- `CLM_DATABASE_URL`
- `CLM_DATABASE_USERNAME`
- `CLM_DATABASE_PASSWORD`
- `PORT`

The default database values match `docker-compose.yml`.
