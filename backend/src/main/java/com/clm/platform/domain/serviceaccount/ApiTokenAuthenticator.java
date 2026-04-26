package com.clm.platform.domain.serviceaccount;

import java.time.Clock;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clm.platform.security.AuthorityNames;
import com.clm.platform.security.BuiltInRole;
import com.clm.platform.security.Permission;
import com.clm.platform.security.ServiceAccountPrincipal;

@Service
public class ApiTokenAuthenticator {

	private final ApiTokenRepository apiTokenRepository;

	private final ServiceAccountRepository serviceAccountRepository;

	private final ServiceAccountService serviceAccountService;

	private final TokenHasher tokenHasher;

	private final Clock clock;

	public ApiTokenAuthenticator(
		ApiTokenRepository apiTokenRepository,
		ServiceAccountRepository serviceAccountRepository,
		ServiceAccountService serviceAccountService,
		TokenHasher tokenHasher,
		Clock clock) {
		this.apiTokenRepository = apiTokenRepository;
		this.serviceAccountRepository = serviceAccountRepository;
		this.serviceAccountService = serviceAccountService;
		this.tokenHasher = tokenHasher;
		this.clock = clock;
	}

	@Transactional
	public Optional<Authentication> authenticate(String token) {
		return apiTokenRepository.findByTokenHash(tokenHasher.hash(token))
			.filter(apiToken -> apiToken.activeAt(clock.instant()))
			.flatMap(apiToken -> serviceAccountRepository.findById(apiToken.serviceAccountId())
				.filter(serviceAccount -> serviceAccount.status() == ServiceAccountStatus.ACTIVE)
				.map(serviceAccount -> authentication(serviceAccount, apiToken)));
	}

	private Authentication authentication(ServiceAccount serviceAccount, ApiToken apiToken) {
		Set<Permission> scopes = serviceAccountService.parseScopes(apiToken.scopes());
		apiToken.markUsed(clock.instant());
		var authorities = scopes.stream()
			.map(AuthorityNames::permission)
			.map(SimpleGrantedAuthority::new)
			.<GrantedAuthority>map(authority -> authority)
			.collect(java.util.stream.Collectors.toSet());
		authorities.add(new SimpleGrantedAuthority(AuthorityNames.role(BuiltInRole.SERVICE_ACCOUNT)));
		ServiceAccountPrincipal principal = new ServiceAccountPrincipal(
			serviceAccount.id(),
			serviceAccount.tenantId(),
			serviceAccount.name(),
			authorities);
		return new UsernamePasswordAuthenticationToken(principal, "N/A", authorities);
	}
}
