CREATE TABLE tenants (
    id UUID PRIMARY KEY,
    slug VARCHAR(80) NOT NULL,
    name VARCHAR(200) NOT NULL,
    status VARCHAR(32) NOT NULL,
    default_tenant BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_tenants_slug UNIQUE (slug)
);

CREATE TABLE organizations (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants (id),
    slug VARCHAR(80) NOT NULL,
    name VARCHAR(200) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_organizations_tenant_slug UNIQUE (tenant_id, slug)
);

CREATE TABLE user_accounts (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    display_name VARCHAR(200) NOT NULL,
    password_hash VARCHAR(255),
    external_subject VARCHAR(256),
    external_provider VARCHAR(128),
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_user_accounts_email UNIQUE (email)
);

CREATE TABLE user_role_assignments (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES user_accounts (id),
    tenant_id UUID REFERENCES tenants (id),
    organization_id UUID REFERENCES organizations (id),
    role_key VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_user_role_scope UNIQUE (user_id, tenant_id, organization_id, role_key)
);

CREATE TABLE service_accounts (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants (id),
    name VARCHAR(200) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE api_tokens (
    id UUID PRIMARY KEY,
    service_account_id UUID NOT NULL REFERENCES service_accounts (id),
    token_prefix VARCHAR(24) NOT NULL,
    token_hash VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL,
    scopes VARCHAR(2048) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_used_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_api_tokens_hash UNIQUE (token_hash)
);

CREATE INDEX ix_organizations_tenant_id ON organizations (tenant_id);
CREATE INDEX ix_user_role_assignments_user_id ON user_role_assignments (user_id);
CREATE INDEX ix_user_role_assignments_tenant_id ON user_role_assignments (tenant_id);
CREATE INDEX ix_service_accounts_tenant_id ON service_accounts (tenant_id);
CREATE INDEX ix_api_tokens_service_account_id ON api_tokens (service_account_id);
CREATE INDEX ix_api_tokens_prefix ON api_tokens (token_prefix);
