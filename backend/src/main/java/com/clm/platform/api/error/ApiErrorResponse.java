package com.clm.platform.api.error;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
	String code,
	String message,
	String remediation,
	String correlationId,
	String path,
	Instant timestamp,
	List<FieldErrorResponse> details) {
}
