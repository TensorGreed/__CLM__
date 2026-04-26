# Observability Foundation

R1-E02 adds request correlation and structured JSON logs.

## Correlation Header

The platform uses `X-Correlation-ID`.

If a request includes a valid correlation ID, the same value is returned in the response header and added to log MDC as `correlationId`.

If the header is missing or invalid, the API generates a UUID.

## Logs

Backend logs are emitted as JSON through Logback.

Every request-handling log line can include:

- service name
- logger
- level
- message
- thread
- correlation ID when a request is active

## Secret Handling

Do not log secrets, private keys, bearer tokens, connector credentials, or decrypted secret values.

The API error handler and foundation services redact assignment-style sensitive values before returning or persisting user-facing diagnostic text.
