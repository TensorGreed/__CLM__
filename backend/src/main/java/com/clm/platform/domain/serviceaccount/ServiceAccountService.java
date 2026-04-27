package com.clm.platform.domain.serviceaccount;

import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clm.platform.api.error.ResourceNotFoundException;
import com.clm.platform.domain.tenancy.TenancyService;
import com.clm.platform.security.CurrentActor;
import com.clm.platform.security.Permission;

@Service
public class ServiceAccountService {

	private final ServiceAccountRepository serviceAccountRepository;

	private final ApiTokenRepository apiTokenRepository;

	private final TenancyService tenancyService;

	private final TokenGenerator tokenGenerator;

	private final TokenHasher tokenHasher;

	private final Clock clock;

	public ServiceAccountService(
		ServiceAccountRepository serviceAccountRepository,
		ApiTokenRepository apiTokenRepository,
		TenancyService tenancyService,
		TokenGenerator tokenGenerator,
		TokenHasher tokenHasher,
		Clock clock) {
		this.serviceAccountRepository = serviceAccountRepository;
		this.apiTokenRepository = apiTokenRepository;
		this.tenancyService = tenancyService;
		this.tokenGenerator = tokenGenerator;
		this.tokenHasher = tokenHasher;
		this.clock = clock;
	}

	@Transactional
	public ServiceAccount create(ServiceAccountCreateRequest request) {
		tenancyService.getTenant(request.tenantId());
		return serviceAccountRepository.save(new ServiceAccount(UUID.randomUUID(), request.tenantId(), request.name(), clock.instant()));
	}

	@Transactional(readOnly = true)
	public List<ServiceAccount> list(UUID tenantId) {
		if (tenantId != null) {
			tenancyService.getTenant(tenantId);
			return serviceAccountRepository.findByTenantIdOrderByNameAsc(tenantId);
		}
		if (CurrentActor.hasGlobalAccess()) {
			return serviceAccountRepository.findAllByOrderByNameAsc();
		}
		List<UUID> tenantIds = CurrentActor.tenantIds().stream().sorted().toList();
		if (tenantIds.isEmpty()) {
			return List.of();
		}
		return serviceAccountRepository.findByTenantIdInOrderByNameAsc(tenantIds);
	}

	@Transactional(readOnly = true)
	public ServiceAccount get(UUID serviceAccountId) {
		ServiceAccount serviceAccount = serviceAccountRepository.findById(serviceAccountId)
			.orElseThrow(() -> new ResourceNotFoundException("ServiceAccount", serviceAccountId));
		tenancyService.getTenant(serviceAccount.tenantId());
		return serviceAccount;
	}

	@Transactional(readOnly = true)
	public List<ApiTokenResponse> listTokens(UUID serviceAccountId) {
		ServiceAccount serviceAccount = get(serviceAccountId);
		return apiTokenRepository.findByServiceAccountIdOrderByCreatedAtDesc(serviceAccount.id())
			.stream()
			.map(apiToken -> ApiTokenResponse.from(apiToken, parseScopes(apiToken.scopes())))
			.toList();
	}

	@Transactional
	public ApiTokenSecretResponse createToken(UUID serviceAccountId, ApiTokenCreateRequest request) {
		ServiceAccount serviceAccount = get(serviceAccountId);
		TokenGenerator.GeneratedToken generatedToken = tokenGenerator.generate();
		ApiToken apiToken = apiTokenRepository.save(new ApiToken(
			UUID.randomUUID(),
			serviceAccount.id(),
			generatedToken.prefix(),
			tokenHasher.hash(generatedToken.secret()),
			serializeScopes(request.scopes()),
			request.expiresAt(),
			clock.instant()));
		return new ApiTokenSecretResponse(
			apiToken.id(),
			serviceAccount.id(),
			apiToken.tokenPrefix(),
			generatedToken.secret(),
			request.scopes(),
			apiToken.expiresAt());
	}

	@Transactional
	public ApiTokenSecretResponse rotateToken(UUID tokenId) {
		ApiToken apiToken = apiTokenRepository.findById(tokenId)
			.orElseThrow(() -> new ResourceNotFoundException("ApiToken", tokenId));
		TokenGenerator.GeneratedToken generatedToken = tokenGenerator.generate();
		apiToken.rotate(generatedToken.prefix(), tokenHasher.hash(generatedToken.secret()), clock.instant());
		return new ApiTokenSecretResponse(
			apiToken.id(),
			apiToken.serviceAccountId(),
			apiToken.tokenPrefix(),
			generatedToken.secret(),
			parseScopes(apiToken.scopes()),
			apiToken.expiresAt());
	}

	@Transactional
	public ApiTokenResponse revokeToken(UUID tokenId) {
		ApiToken apiToken = apiTokenRepository.findById(tokenId)
			.orElseThrow(() -> new ResourceNotFoundException("ApiToken", tokenId));
		apiToken.revoke();
		return ApiTokenResponse.from(apiToken, parseScopes(apiToken.scopes()));
	}

	public Set<Permission> parseScopes(String scopes) {
		return Arrays.stream(scopes.split(","))
			.filter(scope -> !scope.isBlank())
			.map(Permission::valueOf)
			.collect(Collectors.toUnmodifiableSet());
	}

	private String serializeScopes(Set<Permission> scopes) {
		return scopes.stream().map(Permission::name).sorted().collect(Collectors.joining(","));
	}
}
