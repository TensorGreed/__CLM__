# Worker Foundation

R1-E02 adds an async task run foundation without implementing lifecycle business jobs.

## Task Run Fields

- `id`
- `idempotencyKey`
- `taskType`
- `status`
- `attempts`
- `maxAttempts`
- `createdAt`
- `updatedAt`
- `startedAt`
- `completedAt`
- `nextRunAt`
- `lastErrorCode`
- `lastErrorMessage`
- `correlationId`
- `inputSummary`
- `resultSummary`

## Statuses

- `QUEUED`
- `RUNNING`
- `RETRY_SCHEDULED`
- `SUCCEEDED`
- `FAILED`
- `CANCELED`

## Idempotency

Task creation is idempotent by `idempotencyKey`.

The runner will not re-execute terminal tasks.

## Task Detail API

Use:

```bash
curl http://localhost:8080/api/v1/tasks/{taskId}
```

The response is redacted task state only. No raw secrets or private key material should be stored in task summaries.
