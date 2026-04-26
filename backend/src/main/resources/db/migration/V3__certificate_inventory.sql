CREATE TABLE certificates (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants (id),
    owner VARCHAR(256),
    orphaned BOOLEAN NOT NULL,
    status VARCHAR(32) NOT NULL,
    current_version_id UUID,
    common_name VARCHAR(256),
    subject_dn TEXT NOT NULL,
    issuer_dn TEXT NOT NULL,
    serial_number VARCHAR(128) NOT NULL,
    not_before TIMESTAMP WITH TIME ZONE NOT NULL,
    not_after TIMESTAMP WITH TIME ZONE NOT NULL,
    sha256_fingerprint VARCHAR(95) NOT NULL,
    sha1_fingerprint VARCHAR(59) NOT NULL,
    sans TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_certificates_tenant_sha256 UNIQUE (tenant_id, sha256_fingerprint),
    CONSTRAINT ck_certificates_owner_or_orphaned CHECK (owner IS NOT NULL OR orphaned = TRUE)
);

CREATE TABLE certificate_versions (
    id UUID PRIMARY KEY,
    certificate_id UUID NOT NULL REFERENCES certificates (id),
    tenant_id UUID NOT NULL REFERENCES tenants (id),
    version_number INTEGER NOT NULL,
    source VARCHAR(32) NOT NULL,
    certificate_pem TEXT NOT NULL,
    subject_dn TEXT NOT NULL,
    issuer_dn TEXT NOT NULL,
    serial_number VARCHAR(128) NOT NULL,
    not_before TIMESTAMP WITH TIME ZONE NOT NULL,
    not_after TIMESTAMP WITH TIME ZONE NOT NULL,
    sha256_fingerprint VARCHAR(95) NOT NULL,
    sha1_fingerprint VARCHAR(59) NOT NULL,
    public_key_algorithm VARCHAR(64) NOT NULL,
    signature_algorithm VARCHAR(128) NOT NULL,
    sans TEXT NOT NULL,
    chain_length INTEGER NOT NULL,
    self_signed BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_certificate_versions_certificate_number UNIQUE (certificate_id, version_number),
    CONSTRAINT uk_certificate_versions_tenant_sha256 UNIQUE (tenant_id, sha256_fingerprint)
);

ALTER TABLE certificates
    ADD CONSTRAINT fk_certificates_current_version
    FOREIGN KEY (current_version_id) REFERENCES certificate_versions (id);

CREATE TABLE certificate_chain_entries (
    id UUID PRIMARY KEY,
    certificate_version_id UUID NOT NULL REFERENCES certificate_versions (id),
    position INTEGER NOT NULL,
    subject_dn TEXT NOT NULL,
    issuer_dn TEXT NOT NULL,
    serial_number VARCHAR(128) NOT NULL,
    not_before TIMESTAMP WITH TIME ZONE NOT NULL,
    not_after TIMESTAMP WITH TIME ZONE NOT NULL,
    sha256_fingerprint VARCHAR(95) NOT NULL,
    sha1_fingerprint VARCHAR(59) NOT NULL,
    self_signed BOOLEAN NOT NULL,
    CONSTRAINT uk_certificate_chain_entries_position UNIQUE (certificate_version_id, position)
);

CREATE TABLE certificate_tags (
    certificate_id UUID NOT NULL REFERENCES certificates (id),
    tag VARCHAR(64) NOT NULL,
    PRIMARY KEY (certificate_id, tag)
);

CREATE INDEX ix_certificates_tenant_status ON certificates (tenant_id, status);
CREATE INDEX ix_certificates_tenant_owner ON certificates (tenant_id, owner);
CREATE INDEX ix_certificates_tenant_issuer ON certificates (tenant_id, issuer_dn);
CREATE INDEX ix_certificates_tenant_not_after ON certificates (tenant_id, not_after);
CREATE INDEX ix_certificates_current_version_id ON certificates (current_version_id);
CREATE INDEX ix_certificate_versions_certificate_id ON certificate_versions (certificate_id);
CREATE INDEX ix_certificate_versions_tenant_not_after ON certificate_versions (tenant_id, not_after);
CREATE INDEX ix_certificate_chain_entries_version_id ON certificate_chain_entries (certificate_version_id);
CREATE INDEX ix_certificate_tags_tag ON certificate_tags (tag);
