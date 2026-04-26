package com.clm.platform.worker;

import java.util.UUID;

public record TaskRunContext(
	UUID taskId,
	String taskType,
	String idempotencyKey,
	String correlationId,
	String inputSummary) {
}
