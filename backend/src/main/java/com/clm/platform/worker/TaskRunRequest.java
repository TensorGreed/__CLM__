package com.clm.platform.worker;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskRunRequest(
	@NotBlank @Size(max = 200) String idempotencyKey,
	@NotBlank @Size(max = 128) String taskType,
	@Min(1) @Max(10) Integer maxAttempts,
	@Size(max = 4096) String inputSummary) {

	int resolvedMaxAttempts() {
		return maxAttempts == null ? 3 : maxAttempts;
	}
}
