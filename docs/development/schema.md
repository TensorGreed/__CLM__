# Schema And Migrations

Flyway manages database schema changes.

## Baseline Migration

`V1__platform_foundation.sql` creates:

- `audit_events`
- `task_runs`

The migration is intentionally limited to platform foundation tables.

## Identity Migration

`V2__identity_tenancy_rbac.sql` creates:

- `tenants`
- `organizations`
- `user_accounts`
- `user_role_assignments`
- `service_accounts`
- `api_tokens`

The migration includes indexes for tenant joins, role lookups, service account lookups, and token lookup metadata.

## Certificate Inventory Migration

`V3__certificate_inventory.sql` creates:

- `certificates`
- `certificate_versions`
- `certificate_chain_entries`
- `certificate_tags`

`certificates` is the tenant-scoped system-of-record row. `certificate_versions` is append-oriented and deduplicated by tenant plus SHA-256 fingerprint. `certificate_chain_entries` stores parsed public chain metadata for imported versions. `certificate_tags` stores normalized inventory tags for filtering.

The migration adds indexes for tenant/status, owner, issuer, expiry, current version lookup, version history lookup, chain lookup, and tag lookup. The schema intentionally does not store private key material in R1-E04.

## Certificate Inventory Management Migration

`V4__certificate_inventory_management.sql` creates:

- `certificate_source_observations`
- `certificate_metadata_entries`
- `certificate_status_history`

Source observations record which connector, discovery source, or future automation observed a public certificate version. Repeated observations from the same source identity update `last_seen_at` and `observation_count` instead of creating duplicates.

Metadata entries store typed custom metadata values with one row per certificate and metadata key. Status history stores explicit transitions, actor identifiers, reasons, and timestamps.

The migration adds indexes for source lookup, certificate observation lookup, metadata key lookup, and status-history timelines.

## Test Profile

The `test` profile runs Flyway against H2 in PostgreSQL compatibility mode and validates JPA mappings with `ddl-auto: validate`.

## Rules For Future Migrations

- Add one migration per cohesive schema change.
- Keep migrations deterministic and repeatable.
- Prefer additive changes where possible.
- Add indexes for list filters and joins introduced by the story.
- Document rollback considerations in the pull request.
