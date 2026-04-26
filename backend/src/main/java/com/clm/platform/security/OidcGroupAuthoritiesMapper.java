package com.clm.platform.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.stereotype.Component;

@Component
public class OidcGroupAuthoritiesMapper {

	private final OidcProperties oidcProperties;

	public OidcGroupAuthoritiesMapper(OidcProperties oidcProperties) {
		this.oidcProperties = oidcProperties;
	}

	public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
		Set<GrantedAuthority> mapped = new HashSet<>(authorities);
		for (GrantedAuthority authority : authorities) {
			if (authority instanceof OidcUserAuthority oidcUserAuthority) {
				mapped.addAll(mapGroups(oidcUserAuthority));
			}
		}
		return mapped;
	}

	private Set<GrantedAuthority> mapGroups(OidcUserAuthority authority) {
		Set<GrantedAuthority> mapped = new HashSet<>();
		Object groupsClaim = authority.getUserInfo() == null
			? authority.getIdToken().getClaims().get(oidcProperties.resolvedGroupClaim())
			: authority.getUserInfo().getClaims().get(oidcProperties.resolvedGroupClaim());

		if (groupsClaim instanceof Collection<?> groups) {
			for (Object group : groups) {
				BuiltInRole role = oidcProperties.resolvedGroupRoleMappings().get(String.valueOf(group));
				if (role != null) {
					mapped.add(new SimpleGrantedAuthority(AuthorityNames.role(role)));
					role.permissions().stream()
						.map(AuthorityNames::permission)
						.map(SimpleGrantedAuthority::new)
						.forEach(mapped::add);
				}
			}
		}
		return mapped;
	}
}
