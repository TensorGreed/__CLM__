package com.clm.platform.worker;

import java.time.Instant;
import java.util.UUID;

public record TaskRunResponse(
	UUID id,
	String taskType,
	TaskStatus status,
	int attempts,
	int maxAttempts,
	Instant createdAt,
	Instant updatedAt,
	Instant startedAt,
	Instant completedAt,
	Instant nextRunAt,
	String lastErrorCode,
	String lastErrorMessage,
	String correlationId,
	String inputSummary,
	String resultSummary) {

	static TaskRunResponse from(TaskRun taskRun) {
		return new TaskRunResponse(
			taskRun.id(),
			taskRun.taskType(),
			taskRun.status(),
			taskRun.attempts(),
			taskRun.maxAttempts(),
			taskRun.createdAt(),
			taskRun.updatedAt(),
			taskRun.startedAt(),
			taskRun.completedAt(),
			taskRun.nextRunAt(),
			taskRun.lastErrorCode(),
			taskRun.lastErrorMessage(),
			taskRun.correlationId(),
			taskRun.inputSummary(),
			taskRun.resultSummary());
	}
}
