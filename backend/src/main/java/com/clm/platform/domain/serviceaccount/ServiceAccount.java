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
@Table(name = "service_accounts")
public class ServiceAccount {

	@Id
	private UUID id;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Column(nullable = false, length = 200)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private ServiceAccountStatus status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected ServiceAccount() {
	}

	public ServiceAccount(UUID id, UUID tenantId, String name, Instant now) {
		this.id = id;
		this.tenantId = tenantId;
		this.name = name;
		this.status = ServiceAccountStatus.ACTIVE;
		this.createdAt = now;
		this.updatedAt = now;
	}

	public UUID id() {
		return id;
	}

	public UUID tenantId() {
		return tenantId;
	}

	public String name() {
		return name;
	}

	public ServiceAccountStatus status() {
		return status;
	}
}
