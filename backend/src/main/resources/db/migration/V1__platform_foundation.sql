CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    actor_type VARCHAR(64) NOT NULL,
    actor_id VARCHAR(256),
    tenant_id VARCHAR(128),
    action VARCHAR(128) NOT NULL,
    resource_type VARCHAR(128) NOT NULL,
    resource_id VARCHAR(256),
    decision VARCHAR(64) NOT NULL,
    status VARCHAR(64) NOT NULL,
    reason VARCHAR(1024),
    correlation_id VARCHAR(128),
    metadata TEXT
);

CREATE INDEX ix_audit_events_occurred_at ON audit_events (occurred_at);
CREATE INDEX ix_audit_events_correlation_id ON audit_events (correlation_id);
CREATE INDEX ix_audit_events_resource ON audit_events (resource_type, resource_id);
CREATE INDEX ix_audit_events_tenant_occurred_at ON audit_events (tenant_id, occurred_at);

CREATE TABLE task_runs (
    id UUID PRIMARY KEY,
    idempotency_key VARCHAR(200) NOT NULL,
    task_type VARCHAR(128) NOT NULL,
    status VARCHAR(64) NOT NULL,
    attempts INTEGER NOT NULL,
    max_attempts INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    next_run_at TIMESTAMP WITH TIME ZONE,
    last_error_code VARCHAR(128),
    last_error_message VARCHAR(1024),
    correlation_id VARCHAR(128),
    input_summary TEXT,
    result_summary TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_task_runs_idempotency_key UNIQUE (idempotency_key)
);

CREATE INDEX ix_task_runs_status_next_run_at ON task_runs (status, next_run_at);
CREATE INDEX ix_task_runs_task_type ON task_runs (task_type);
CREATE INDEX ix_task_runs_correlation_id ON task_runs (correlation_id);
