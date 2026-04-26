package com.clm.platform.domain.serviceaccount;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ServiceAccountCreateRequest(
	@NotNull UUID tenantId,
	@NotBlank @Size(max = 200) String name) {
}
