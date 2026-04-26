# Schema And Migrations

Flyway manages database schema changes.

## Baseline Migration

`V1__platform_foundation.sql` creates:

- `audit_events`
- `task_runs`

The migration is intentionally limited to platform foundation tables. Certificate inventory schema starts in R1-E04.

## Test Profile

The `test` profile runs Flyway against H2 in PostgreSQL compatibility mode and validates JPA mappings with `ddl-auto: validate`.

## Rules For Future Migrations

- Add one migration per cohesive schema change.
- Keep migrations deterministic and repeatable.
- Prefer additive changes where possible.
- Add indexes for list filters and joins introduced by the story.
- Document rollback considerations in the pull request.
