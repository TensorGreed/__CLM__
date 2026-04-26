# Identity, Tenancy, And RBAC

R1-E03 adds the first secure access layer for the backend.

## Bootstrap Admin

A fresh deployment exposes:

- `GET /api/v1/bootstrap/status`
- `POST /api/v1/bootstrap/admin`

Bootstrap is available only while no user accounts exist. The admin request creates:

- A default tenant.
- A default organization.
- A local administrator account.
- A global admin role assignment.
- An audit event for the bootstrap action.

After the first account exists, additional bootstrap attempts return `CONFLICT`.

## Authentication Modes

The backend supports:

- HTTP Basic for local bootstrapped users.
- Bearer tokens for service accounts.
- Optional OIDC login when `CLM_OIDC_ENABLED=true`.

OIDC uses Spring OAuth client registration settings. Group-to-role mapping reads the claim configured by `CLM_OIDC_GROUP_CLAIM`, defaulting to `groups`.

Example role mapping property shape:

```yaml
clm:
  identity:
    oidc:
      enabled: true
      group-claim: groups
      group-role-mappings:
        clm-admins: ADMIN
        clm-auditors: AUDITOR
```

## Built-In Roles

The current built-in roles are:

- `ADMIN`
- `OPERATOR`
- `REQUESTER`
- `APPROVER`
- `AUDITOR`
- `READ_ONLY`
- `SERVICE_ACCOUNT`

Roles expand into `ROLE_*` and `PERMISSION_*` authorities at authentication time. The role and permission matrix is exposed at:

- `GET /api/v1/roles`
- `GET /api/v1/permissions`

Both endpoints require `PERMISSION_ROLE_READ`.

## Tenants And Organizations

Tenant APIs:

- `GET /api/v1/tenants`
- `POST /api/v1/tenants`
- `GET /api/v1/tenants/{tenantId}`
- `PUT /api/v1/tenants/{tenantId}`

Organization APIs:

- `GET /api/v1/tenants/{tenantId}/organizations`
- `POST /api/v1/tenants/{tenantId}/organizations`
- `PUT /api/v1/tenants/{tenantId}/organizations/{organizationId}`

Tenant and organization APIs enforce permission checks and tenant scope. Global administrators can see and manage all tenants. Tenant-scoped users and service accounts only see their assigned tenant.

## Sensitive Actions

Sensitive actions use:

- `POST /api/v1/sensitive-actions`

The request requires an action type, resource type, resource ID, and reason. The reason must be at least ten characters. Successful requests append an audit event using the current actor.

Current action types:

- `EXPORT_PRIVATE_KEY`
- `REVOKE_CERTIFICATE`
- `APPROVE_EXCEPTION`
- `ROTATE_CERTIFICATE`
- `DELETE_RESOURCE`
- `BREAK_GLASS`

## Current Limits

R1-E03 intentionally does not add user management screens, SCIM, SAML, ABAC, certificate inventory, or frontend business workflows. OIDC users receive authorities from group mapping, but persistent OIDC user provisioning is reserved for later governance work.
