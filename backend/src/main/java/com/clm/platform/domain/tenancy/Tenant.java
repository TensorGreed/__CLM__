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
@Table(name = "tenants")
public class Tenant {

	@Id
	private UUID id;

	@Column(nullable = false, unique = true, length = 80)
	private String slug;

	@Column(nullable = false, length = 200)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private ResourceStatus status;

	@Column(name = "default_tenant", nullable = false)
	private boolean defaultTenant;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected Tenant() {
	}

	private Tenant(UUID id, String slug, String name, boolean defaultTenant, Instant now) {
		this.id = id;
		this.slug = slug;
		this.name = name;
		this.defaultTenant = defaultTenant;
		this.status = ResourceStatus.ACTIVE;
		this.createdAt = now;
		this.updatedAt = now;
	}

	public static Tenant createDefault(String slug, String name, Instant now) {
		return new Tenant(UUID.randomUUID(), slug, name, true, now);
	}

	public static Tenant create(String slug, String name, Instant now) {
		return new Tenant(UUID.randomUUID(), slug, name, false, now);
	}

	public void update(String name, ResourceStatus status, Instant now) {
		this.name = name;
		this.status = status;
		this.updatedAt = now;
	}

	public UUID id() {
		return id;
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

	public boolean defaultTenant() {
		return defaultTenant;
	}

	public Instant createdAt() {
		return createdAt;
	}

	public Instant updatedAt() {
		return updatedAt;
	}
}
