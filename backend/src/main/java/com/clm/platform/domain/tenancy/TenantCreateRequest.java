package com.clm.platform.domain.tenancy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TenantCreateRequest(
	@NotBlank @Size(max = 80) @Pattern(regexp = "[a-z0-9][a-z0-9-]*") String slug,
	@NotBlank @Size(max = 200) String name) {
}
