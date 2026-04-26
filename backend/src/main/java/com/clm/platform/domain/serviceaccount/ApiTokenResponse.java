package com.clm.platform.domain.serviceaccount;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.clm.platform.security.Permission;

public record ApiTokenResponse(
	UUID id,
	UUID serviceAccountId,
	String tokenPrefix,
	ApiTokenStatus status,
	Set<Permission> scopes,
	Instant expiresAt) {

	static ApiTokenResponse from(ApiToken apiToken, Set<Permission> scopes) {
		return new ApiTokenResponse(
			apiToken.id(),
			apiToken.serviceAccountId(),
			apiToken.tokenPrefix(),
			apiToken.status(),
			scopes,
			apiToken.expiresAt());
	}
}
