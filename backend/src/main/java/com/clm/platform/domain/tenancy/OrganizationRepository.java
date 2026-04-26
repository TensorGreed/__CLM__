package com.clm.platform.domain.tenancy;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

	boolean existsByTenantIdAndSlug(UUID tenantId, String slug);

	List<Organization> findByTenantId(UUID tenantId);
}
