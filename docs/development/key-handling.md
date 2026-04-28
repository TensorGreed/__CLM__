# Key Handling Guardrails

R2-E01 starts private key handling with rejection and policy controls before any key material is persisted.

## Defaults

Private key persistence is disabled by default.

Runtime placeholders:

- `CLM_PRIVATE_KEY_IMPORT_ENABLED=false`
- `CLM_KEY_STORAGE_PROVIDER=disabled`
- `CLM_PRIVATE_KEY_DATABASE_PERSISTENCE_ENABLED=false`

These settings define the future policy surface. They do not enable real secret storage yet.

## Public Certificate Import

`POST /api/v1/certificates/import` is public-certificate-only.

The API accepts a leaf certificate and optional public chain. It rejects private key PEM blocks in `certificatePem` and `chainPem`, and it never accepts private key fields on this path.

## Future Private Key Import Path

`POST /api/v1/certificates/import-with-private-key` requires `PERMISSION_CERTIFICATE_IMPORT`.

The endpoint is intentionally disabled by default. It validates these guardrails before returning the policy rejection:

- `certificatePem` must be a public leaf certificate.
- `chainPem` must contain only public certificate blocks when provided.
- `privateKeyPem` must contain a private key PEM block.
- No private key body is stored, logged, returned, or written into audit metadata.

Authorized attempts rejected by policy emit `certificate.private_key_import_rejected` audit events with `DENY` and `FAILURE`.

## Parsed Public Key Metadata

Certificate versions now store `publicKeySizeBits` when it can be derived safely from the public key embedded in the certificate. RSA, EC, and DSA key sizes are supported.

## Redaction

Diagnostic text redacts assignment-style secrets and full private key PEM blocks before API error responses, audit reasons, audit metadata, task summaries, or task errors are persisted.

Later R2-E01 stories must add key-match validation, approved external secret references, non-exportable key handling, export policy, and bulk import workflows before private key import can be enabled.
