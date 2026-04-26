package com.clm.platform.domain.tenancy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TenantUpdateRequest(
	@NotBlank @Size(max = 200) String name,
	@NotNull ResourceStatus status) {
}
