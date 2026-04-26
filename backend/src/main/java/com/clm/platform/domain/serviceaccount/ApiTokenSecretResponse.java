package com.clm.platform.domain.serviceaccount;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.clm.platform.security.Permission;

public record ApiTokenSecretResponse(
	UUID id,
	UUID serviceAccountId,
	String tokenPrefix,
	String token,
	Set<Permission> scopes,
	Instant expiresAt) {
}
