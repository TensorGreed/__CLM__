package com.clm.platform.domain.identity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.clm.platform.security.BuiltInRole;

@Entity
@Table(name = "user_role_assignments")
public class UserRoleAssignment {

	@Id
	private UUID id;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "tenant_id")
	private UUID tenantId;

	@Column(name = "organization_id")
	private UUID organizationId;

	@Enumerated(EnumType.STRING)
	@Column(name = "role_key", nullable = false, length = 64)
	private BuiltInRole role;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected UserRoleAssignment() {
	}

	public UserRoleAssignment(UUID id, UUID userId, UUID tenantId, UUID organizationId, BuiltInRole role, Instant createdAt) {
		this.id = id;
		this.userId = userId;
		this.tenantId = tenantId;
		this.organizationId = organizationId;
		this.role = role;
		this.createdAt = createdAt;
	}

	public BuiltInRole role() {
		return role;
	}

	public UUID tenantId() {
		return tenantId;
	}
}
