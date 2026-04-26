package com.clm.platform.domain.serviceaccount;

import java.time.Instant;
import java.util.Set;

import jakarta.validation.constraints.NotEmpty;

import com.clm.platform.security.Permission;

public record ApiTokenCreateRequest(
	@NotEmpty Set<Permission> scopes,
	Instant expiresAt) {
}
