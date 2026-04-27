package com.clm.platform.domain.serviceaccount;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.clm.platform.api.ApiPaths;

@RestController
public class ServiceAccountController {

	private final ServiceAccountService serviceAccountService;

	public ServiceAccountController(ServiceAccountService serviceAccountService) {
		this.serviceAccountService = serviceAccountService;
	}

	@GetMapping(ApiPaths.API_V1 + "/service-accounts")
	@PreAuthorize("hasAuthority('PERMISSION_SERVICE_ACCOUNT_READ')")
	List<ServiceAccountResponse> list(@RequestParam(required = false) UUID tenantId) {
		return serviceAccountService.list(tenantId).stream().map(ServiceAccountResponse::from).toList();
	}

	@PostMapping(ApiPaths.API_V1 + "/service-accounts")
	@PreAuthorize("hasAuthority('PERMISSION_SERVICE_ACCOUNT_MANAGE')")
	ServiceAccountResponse create(@Valid @RequestBody ServiceAccountCreateRequest request) {
		return ServiceAccountResponse.from(serviceAccountService.create(request));
	}

	@GetMapping(ApiPaths.API_V1 + "/service-accounts/{serviceAccountId}/tokens")
	@PreAuthorize("hasAuthority('PERMISSION_SERVICE_ACCOUNT_READ')")
	List<ApiTokenResponse> listTokens(@PathVariable UUID serviceAccountId) {
		return serviceAccountService.listTokens(serviceAccountId);
	}

	@PostMapping(ApiPaths.API_V1 + "/service-accounts/{serviceAccountId}/tokens")
	@PreAuthorize("hasAuthority('PERMISSION_SERVICE_ACCOUNT_MANAGE')")
	ApiTokenSecretResponse createToken(
			@PathVariable UUID serviceAccountId,
			@Valid @RequestBody ApiTokenCreateRequest request) {
		return serviceAccountService.createToken(serviceAccountId, request);
	}

	@PostMapping(ApiPaths.API_V1 + "/api-tokens/{tokenId}/rotate")
	@PreAuthorize("hasAuthority('PERMISSION_SERVICE_ACCOUNT_MANAGE')")
	ApiTokenSecretResponse rotateToken(@PathVariable UUID tokenId) {
		return serviceAccountService.rotateToken(tokenId);
	}

	@PostMapping(ApiPaths.API_V1 + "/api-tokens/{tokenId}/revoke")
	@PreAuthorize("hasAuthority('PERMISSION_SERVICE_ACCOUNT_MANAGE')")
	ApiTokenResponse revokeToken(@PathVariable UUID tokenId) {
		return serviceAccountService.revokeToken(tokenId);
	}
}
