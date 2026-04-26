package com.clm.platform.security;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "clm.identity.oidc")
public record OidcProperties(
	boolean enabled,
	String groupClaim,
	Map<String, BuiltInRole> groupRoleMappings) {

	public String resolvedGroupClaim() {
		return groupClaim == null || groupClaim.isBlank() ? "groups" : groupClaim;
	}

	public Map<String, BuiltInRole> resolvedGroupRoleMappings() {
		return groupRoleMappings == null ? Map.of() : groupRoleMappings;
	}
}
