package com.clm.platform.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;

class OidcGroupAuthoritiesMapperTests {

	@Test
	void mapsConfiguredOidcGroupsToBuiltInRoleAuthorities() {
		OidcProperties properties = new OidcProperties(
			true,
			"groups",
			Map.of("clm-admins", BuiltInRole.ADMIN, "clm-auditors", BuiltInRole.AUDITOR));
		OidcGroupAuthoritiesMapper mapper = new OidcGroupAuthoritiesMapper(properties);
		OidcIdToken idToken = new OidcIdToken(
			"id-token",
			Instant.now(),
			Instant.now().plusSeconds(300),
			Map.of("sub", "oidc-user-1", "groups", List.of("clm-auditors")));

		var mapped = mapper.mapAuthorities(List.of(
			new OidcUserAuthority(idToken),
			new SimpleGrantedAuthority("OIDC_USER")));

		assertThat(mapped)
			.extracting(authority -> authority.getAuthority())
			.contains("OIDC_USER", "ROLE_AUDITOR", "PERMISSION_AUDIT_READ", "PERMISSION_TENANT_READ");
	}
}
