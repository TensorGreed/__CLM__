package com.clm.platform.domain.tenancy;

import java.time.Instant;
import java.util.UUID;

public record TenantResponse(
	UUID id,
	String slug,
	String name,
	ResourceStatus status,
	boolean defaultTenant,
	Instant createdAt,
	Instant updatedAt) {

	static TenantResponse from(Tenant tenant) {
		return new TenantResponse(
			tenant.id(),
			tenant.slug(),
			tenant.name(),
			tenant.status(),
			tenant.defaultTenant(),
			tenant.createdAt(),
			tenant.updatedAt());
	}
}
