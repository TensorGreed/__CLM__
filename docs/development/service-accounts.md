# Service Accounts And API Tokens

R1-E03 adds scoped automation credentials for backend APIs.

## Model

A service account belongs to one tenant. API tokens belong to a service account and store only:

- Token prefix.
- Token hash.
- Status.
- Permission scopes.
- Expiration timestamp.
- Last-used timestamp.

Raw token secrets are returned only once when a token is created or rotated.

## Endpoints

- `POST /api/v1/service-accounts`
- `POST /api/v1/service-accounts/{serviceAccountId}/tokens`
- `POST /api/v1/api-tokens/{tokenId}/rotate`
- `POST /api/v1/api-tokens/{tokenId}/revoke`

These endpoints require `PERMISSION_SERVICE_ACCOUNT_MANAGE`.

## Token Use

Use a token with the bearer scheme:

```bash
curl -H "Authorization: Bearer clm_..." http://localhost:8080/api/v1/tenants
```

The token scope is converted into `PERMISSION_*` authorities. Service account principals are still tenant-scoped to the service account tenant.

## Rotation And Revocation

Rotating a token replaces the stored hash and invalidates the old secret. Revoking a token changes its status to `REVOKED`; revoked and expired tokens cannot authenticate.

## Security Notes

- Token values are generated with secure random bytes.
- Token hashes use SHA-256 and never store the raw secret.
- Token values must not be logged, stored in issue trackers, or embedded in source code.
- Create tokens with the smallest permission set needed for the automation.
