CREATE TABLE certificate_key_references (
    id UUID PRIMARY KEY,
    certificate_id UUID NOT NULL REFERENCES certificates (id),
    certificate_version_id UUID NOT NULL REFERENCES certificate_versions (id),
    tenant_id UUID NOT NULL REFERENCES tenants (id),
    provider_type VARCHAR(64) NOT NULL,
    reference_uri VARCHAR(512) NOT NULL,
    key_alias VARCHAR(128),
    key_algorithm VARCHAR(32) NOT NULL,
    key_match_verified BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by VARCHAR(256),
    CONSTRAINT uk_certificate_key_references_version UNIQUE (certificate_version_id),
    CONSTRAINT ck_certificate_key_references_verified CHECK (key_match_verified = TRUE)
);

CREATE INDEX ix_certificate_key_references_certificate_id
    ON certificate_key_references (certificate_id);
CREATE INDEX ix_certificate_key_references_tenant_provider
    ON certificate_key_references (tenant_id, provider_type);
