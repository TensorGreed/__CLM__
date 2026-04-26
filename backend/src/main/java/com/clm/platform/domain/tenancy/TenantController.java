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
public class TenantController {

	private final TenancyService tenancyService;

	public TenantController(TenancyService tenancyService) {
		this.tenancyService = tenancyService;
	}

	@GetMapping(ApiPaths.API_V1 + "/tenants")
	@PreAuthorize("hasAuthority('PERMISSION_TENANT_READ')")
	List<TenantResponse> listTenants() {
		return tenancyService.listTenants().stream().map(TenantResponse::from).toList();
	}

	@PostMapping(ApiPaths.API_V1 + "/tenants")
	@PreAuthorize("hasAuthority('PERMISSION_TENANT_MANAGE')")
	TenantResponse createTenant(@Valid @RequestBody TenantCreateRequest request) {
		return TenantResponse.from(tenancyService.createTenant(request));
	}

	@GetMapping(ApiPaths.API_V1 + "/tenants/{tenantId}")
	@PreAuthorize("hasAuthority('PERMISSION_TENANT_READ')")
	TenantResponse getTenant(@PathVariable UUID tenantId) {
		return TenantResponse.from(tenancyService.getTenant(tenantId));
	}

	@PutMapping(ApiPaths.API_V1 + "/tenants/{tenantId}")
	@PreAuthorize("hasAuthority('PERMISSION_TENANT_MANAGE')")
	TenantResponse updateTenant(@PathVariable UUID tenantId, @Valid @RequestBody TenantUpdateRequest request) {
		return TenantResponse.from(tenancyService.updateTenant(tenantId, request));
	}
}
