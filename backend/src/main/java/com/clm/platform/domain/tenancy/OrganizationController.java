package com.clm.platform.domain.tenancy;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.clm.platform.api.ApiPaths;

@RestController
public class OrganizationController {

	private final TenancyService tenancyService;

	public OrganizationController(TenancyService tenancyService) {
		this.tenancyService = tenancyService;
	}

	@GetMapping(ApiPaths.API_V1 + "/tenants/{tenantId}/organizations")
	@PreAuthorize("hasAuthority('PERMISSION_ORGANIZATION_READ')")
	List<OrganizationResponse> listOrganizations(@PathVariable UUID tenantId) {
		return tenancyService.listOrganizations(tenantId).stream().map(OrganizationResponse::from).toList();
	}

	@PostMapping(ApiPaths.API_V1 + "/tenants/{tenantId}/organizations")
	@PreAuthorize("hasAuthority('PERMISSION_ORGANIZATION_MANAGE')")
	OrganizationResponse createOrganization(
			@PathVariable UUID tenantId,
			@Valid @RequestBody OrganizationCreateRequest request) {
		return OrganizationResponse.from(tenancyService.createOrganization(tenantId, request));
	}

	@PutMapping(ApiPaths.API_V1 + "/tenants/{tenantId}/organizations/{organizationId}")
	@PreAuthorize("hasAuthority('PERMISSION_ORGANIZATION_MANAGE')")
	OrganizationResponse updateOrganization(
			@PathVariable UUID tenantId,
			@PathVariable UUID organizationId,
			@Valid @RequestBody OrganizationUpdateRequest request) {
		return OrganizationResponse.from(tenancyService.updateOrganization(tenantId, organizationId, request));
	}
}
