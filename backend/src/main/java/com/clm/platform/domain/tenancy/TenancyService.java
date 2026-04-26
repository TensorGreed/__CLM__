package com.clm.platform.domain.tenancy;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clm.platform.api.error.ConflictException;
import com.clm.platform.api.error.ForbiddenException;
import com.clm.platform.api.error.ResourceNotFoundException;
import com.clm.platform.security.CurrentActor;

@Service
public class TenancyService {

	private final TenantRepository tenantRepository;

	private final OrganizationRepository organizationRepository;

	private final Clock clock;

	public TenancyService(TenantRepository tenantRepository, OrganizationRepository organizationRepository, Clock clock) {
		this.tenantRepository = tenantRepository;
		this.organizationRepository = organizationRepository;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public List<Tenant> listTenants() {
		if (!CurrentActor.hasGlobalAccess()) {
			var scopedTenants = new ArrayList<Tenant>();
			tenantRepository.findAllById(CurrentActor.tenantIds()).forEach(scopedTenants::add);
			return scopedTenants;
		}
		return tenantRepository.findAll();
	}

	@Transactional
	public Tenant createTenant(TenantCreateRequest request) {
		requireGlobalAccess("Creating tenants requires a global administrator.");
		if (tenantRepository.existsBySlug(request.slug())) {
			throw new ConflictException("Tenant slug already exists.");
		}
		return tenantRepository.save(Tenant.create(request.slug(), request.name(), clock.instant()));
	}

	@Transactional(readOnly = true)
	public Tenant getTenant(UUID tenantId) {
		requireTenantAccess(tenantId);
		return tenantRepository.findById(tenantId)
			.orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));
	}

	@Transactional
	public Tenant updateTenant(UUID tenantId, TenantUpdateRequest request) {
		requireTenantAccess(tenantId);
		Tenant tenant = getTenant(tenantId);
		tenant.update(request.name(), request.status(), clock.instant());
		return tenant;
	}

	@Transactional(readOnly = true)
	public List<Organization> listOrganizations(UUID tenantId) {
		requireTenantAccess(tenantId);
		getTenant(tenantId);
		return organizationRepository.findByTenantId(tenantId);
	}

	@Transactional
	public Organization createOrganization(UUID tenantId, OrganizationCreateRequest request) {
		requireTenantAccess(tenantId);
		getTenant(tenantId);
		if (organizationRepository.existsByTenantIdAndSlug(tenantId, request.slug())) {
			throw new ConflictException("Organization slug already exists in tenant.");
		}
		return organizationRepository.save(Organization.create(tenantId, request.slug(), request.name(), clock.instant()));
	}

	@Transactional
	public Organization updateOrganization(UUID tenantId, UUID organizationId, OrganizationUpdateRequest request) {
		requireTenantAccess(tenantId);
		getTenant(tenantId);
		Organization organization = organizationRepository.findById(organizationId)
			.filter(existing -> existing.tenantId().equals(tenantId))
			.orElseThrow(() -> new ResourceNotFoundException("Organization", organizationId));
		organization.update(request.name(), request.status(), clock.instant());
		return organization;
	}

	private void requireTenantAccess(UUID tenantId) {
		if (CurrentActor.hasGlobalAccess()) {
			return;
		}
		if (!CurrentActor.tenantIds().contains(tenantId)) {
			throw new ResourceNotFoundException("Tenant", tenantId);
		}
	}

	private void requireGlobalAccess(String message) {
		if (!CurrentActor.hasGlobalAccess()) {
			throw new ForbiddenException(message);
		}
	}
}
