package com.clm.platform.domain.tenancy;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "organizations")
public class Organization {

	@Id
	private UUID id;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Column(nullable = false, length = 80)
	private String slug;

	@Column(nullable = false, length = 200)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private ResourceStatus status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected Organization() {
	}

	private Organization(UUID id, UUID tenantId, String slug, String name, Instant now) {
		this.id = id;
		this.tenantId = tenantId;
		this.slug = slug;
		this.name = name;
		this.status = ResourceStatus.ACTIVE;
		this.createdAt = now;
		this.updatedAt = now;
	}

	public static Organization create(UUID tenantId, String slug, String name, Instant now) {
		return new Organization(UUID.randomUUID(), tenantId, slug, name, now);
	}

	public void update(String name, ResourceStatus status, Instant now) {
		this.name = name;
		this.status = status;
		this.updatedAt = now;
	}

	public UUID id() {
		return id;
	}

	public UUID tenantId() {
		return tenantId;
	}

	public String slug() {
		return slug;
	}

	public String name() {
		return name;
	}

	public ResourceStatus status() {
		return status;
	}

	public Instant createdAt() {
		return createdAt;
	}

	public Instant updatedAt() {
		return updatedAt;
	}
}
