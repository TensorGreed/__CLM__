package com.clm.platform.domain.serviceaccount;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "api_tokens")
public class ApiToken {

	@Id
	private UUID id;

	@Column(name = "service_account_id", nullable = false)
	private UUID serviceAccountId;

	@Column(name = "token_prefix", nullable = false, length = 24)
	private String tokenPrefix;

	@Column(name = "token_hash", nullable = false, unique = true, length = 128)
	private String tokenHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private ApiTokenStatus status;

	@Column(nullable = false, length = 2048)
	private String scopes;

	@Column(name = "expires_at")
	private Instant expiresAt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "last_used_at")
	private Instant lastUsedAt;

	protected ApiToken() {
	}

	ApiToken(UUID id, UUID serviceAccountId, String tokenPrefix, String tokenHash, String scopes, Instant expiresAt, Instant now) {
		this.id = id;
		this.serviceAccountId = serviceAccountId;
		this.tokenPrefix = tokenPrefix;
		this.tokenHash = tokenHash;
		this.status = ApiTokenStatus.ACTIVE;
		this.scopes = scopes;
		this.expiresAt = expiresAt;
		this.createdAt = now;
	}

	void rotate(String tokenPrefix, String tokenHash, Instant now) {
		this.tokenPrefix = tokenPrefix;
		this.tokenHash = tokenHash;
		this.createdAt = now;
		this.lastUsedAt = null;
		this.status = ApiTokenStatus.ACTIVE;
	}

	void revoke() {
		this.status = ApiTokenStatus.REVOKED;
	}

	void markUsed(Instant now) {
		this.lastUsedAt = now;
	}

	public UUID id() {
		return id;
	}

	public UUID serviceAccountId() {
		return serviceAccountId;
	}

	public String tokenPrefix() {
		return tokenPrefix;
	}

	public String tokenHash() {
		return tokenHash;
	}

	public ApiTokenStatus status() {
		return status;
	}

	public String scopes() {
		return scopes;
	}

	public Instant expiresAt() {
		return expiresAt;
	}

	public boolean activeAt(Instant now) {
		return status == ApiTokenStatus.ACTIVE && (expiresAt == null || expiresAt.isAfter(now));
	}
}
