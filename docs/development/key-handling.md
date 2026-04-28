# Key Handling Guardrails

R2-E01 starts private key handling with rejection, validation, and reference-only controls before any raw key material is persisted.

## Defaults

Private key persistence is disabled by default.

Runtime placeholders:

- `CLM_PRIVATE_KEY_IMPORT_ENABLED=false`
- `CLM_KEY_STORAGE_PROVIDER=disabled`
- `CLM_PRIVATE_KEY_DATABASE_PERSISTENCE_ENABLED=false`

These settings define the policy surface. They do not enable raw database private key persistence or real secret storage provider integrations.

## Public Certificate Import

`POST /api/v1/certificates/import` is public-certificate-only.

The API accepts a leaf certificate and optional public chain. It rejects private key PEM blocks in `certificatePem` and `chainPem`, and it never accepts private key fields on this path.

## Guarded Private Key Import Path

`POST /api/v1/certificates/import-with-private-key` requires `PERMISSION_CERTIFICATE_IMPORT`.

The endpoint is disabled by default. When disabled, it still validates public certificate fields and a safe private-key match before returning the policy rejection. When explicitly enabled with `CLM_PRIVATE_KEY_IMPORT_ENABLED=true`, `CLM_KEY_STORAGE_PROVIDER=external-reference`, and `CLM_PRIVATE_KEY_DATABASE_PERSISTENCE_ENABLED=false`, it stores only an external key reference after validation.

- `certificatePem` must be a public leaf certificate.
- `chainPem` must contain only public certificate blocks when provided.
- `privateKeyPem` must contain an unencrypted PKCS#8 `PRIVATE KEY` block.
- The private key must match the imported certificate public key.
- RSA and EC key match validation are supported.
- Unsupported, encrypted, legacy, mismatched, or malformed keys are rejected with safe validation errors.
- `keyReference` is required when the reference-only path is enabled.
- `keyStorageProvider`, when supplied, must be `external-reference`.
- No private key body is stored, logged, returned, or written into audit metadata.

Authorized validation attempts emit `certificate.private_key_match_validated` or `certificate.private_key_match_failed` audit events. Authorized attempts rejected by policy emit `certificate.private_key_import_rejected` audit events with `DENY` and `FAILURE`.

## External Key References

The reference-only import path records an approved external location for the matching private key without copying the key into the CLM database.

`keyReference` accepts:

- `providerType`: one of `aws-kms`, `aws-secrets-manager`, `azure-key-vault`, `external`, `gcp-secret-manager`, `kubernetes-secret`, or `vault`.
- `referenceUri`: an external secret or key URI, capped at 512 characters and rejected if it contains PEM private key material.
- `keyAlias`: optional display alias.

The stored reference is bound to the certificate version and records the validated key algorithm and `keyMatchVerified=true`.

## Parsed Public Key Metadata

Certificate versions now store `publicKeySizeBits` when it can be derived safely from the public key embedded in the certificate. RSA, EC, and DSA key sizes are supported.

## Redaction

Diagnostic text redacts assignment-style secrets and full private key PEM blocks before API error responses, audit reasons, audit metadata, task summaries, or task errors are persisted.

Later R2-E01 stories must add a provider abstraction for external secret lookups, non-exportable key handling, export policy, chain validation depth, and bulk import workflows before private-key onboarding is considered complete.
