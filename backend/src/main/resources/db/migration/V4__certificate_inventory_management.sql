CREATE TABLE certificate_source_observations (
    id UUID PRIMARY KEY,
    certificate_id UUID NOT NULL REFERENCES certificates (id),
    certificate_version_id UUID NOT NULL REFERENCES certificate_versions (id),
    tenant_id UUID NOT NULL REFERENCES tenants (id),
    source_type VARCHAR(64) NOT NULL,
    source_key VARCHAR(256) NOT NULL,
    observed_resource_key VARCHAR(512) NOT NULL,
    source_name VARCHAR(256),
    sha256_fingerprint VARCHAR(95) NOT NULL,
    metadata TEXT NOT NULL,
    first_seen_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_seen_at TIMESTAMP WITH TIME ZONE NOT NULL,
    observation_count INTEGER NOT NULL,
    CONSTRAINT uk_certificate_source_observation_identity
        UNIQUE (tenant_id, source_type, source_key, observed_resource_key, certificate_version_id),
    CONSTRAINT ck_certificate_source_observation_count CHECK (observation_count > 0)
);

CREATE TABLE certificate_metadata_entries (
    id UUID PRIMARY KEY,
    certificate_id UUID NOT NULL REFERENCES certificates (id),
    tenant_id UUID NOT NULL REFERENCES tenants (id),
    metadata_key VARCHAR(64) NOT NULL,
    value_type VARCHAR(32) NOT NULL,
    value_text TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by VARCHAR(256),
    CONSTRAINT uk_certificate_metadata_entries_key UNIQUE (certificate_id, metadata_key)
);

CREATE TABLE certificate_status_history (
    id UUID PRIMARY KEY,
    certificate_id UUID NOT NULL REFERENCES certificates (id),
    tenant_id UUID NOT NULL REFERENCES tenants (id),
    from_status VARCHAR(32),
    to_status VARCHAR(32) NOT NULL,
    reason VARCHAR(1024),
    changed_by VARCHAR(256),
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX ix_certificate_source_observations_certificate_id
    ON certificate_source_observations (certificate_id);
CREATE INDEX ix_certificate_source_observations_version_id
    ON certificate_source_observations (certificate_version_id);
CREATE INDEX ix_certificate_source_observations_source
    ON certificate_source_observations (tenant_id, source_type, source_key);
CREATE INDEX ix_certificate_source_observations_last_seen
    ON certificate_source_observations (tenant_id, last_seen_at);

CREATE INDEX ix_certificate_metadata_entries_certificate_id
    ON certificate_metadata_entries (certificate_id);
CREATE INDEX ix_certificate_metadata_entries_lookup
    ON certificate_metadata_entries (tenant_id, metadata_key);

CREATE INDEX ix_certificate_status_history_certificate_id
    ON certificate_status_history (certificate_id, changed_at);
CREATE INDEX ix_certificate_status_history_tenant_changed_at
    ON certificate_status_history (tenant_id, changed_at);
