package com.clm.platform.domain.identity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BootstrapAdminRequest(
	@NotBlank @Email @Size(max = 320) String email,
	@NotBlank @Size(max = 200) String displayName,
	@NotBlank @Size(min = 12, max = 256) String password,
	@NotBlank @Size(max = 80) String tenantSlug,
	@NotBlank @Size(max = 200) String tenantName,
	@NotBlank @Size(max = 80) String organizationSlug,
	@NotBlank @Size(max = 200) String organizationName) {
}
