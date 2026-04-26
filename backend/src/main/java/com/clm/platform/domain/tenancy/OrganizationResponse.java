package com.clm.platform.domain.tenancy;

import java.time.Instant;
import java.util.UUID;

public record OrganizationResponse(
	UUID id,
	UUID tenantId,
	String slug,
	String name,
	ResourceStatus status,
	Instant createdAt,
	Instant updatedAt) {

	static OrganizationResponse from(Organization organization) {
		return new OrganizationResponse(
			organization.id(),
			organization.tenantId(),
			organization.slug(),
			organization.name(),
			organization.status(),
			organization.createdAt(),
			organization.updatedAt());
	}
}
