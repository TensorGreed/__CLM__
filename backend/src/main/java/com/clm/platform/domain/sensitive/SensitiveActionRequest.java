package com.clm.platform.domain.sensitive;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SensitiveActionRequest(
	@NotNull SensitiveActionType actionType,
	@NotBlank @Size(max = 128) String resourceType,
	@NotBlank @Size(max = 256) String resourceId,
	@NotBlank @Size(min = 10, max = 1024) String reason) {
}
