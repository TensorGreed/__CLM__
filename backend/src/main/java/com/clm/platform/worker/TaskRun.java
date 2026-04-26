package com.clm.platform.worker;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "task_runs")
public class TaskRun {

	@Id
	private UUID id;

	@Column(name = "idempotency_key", nullable = false, unique = true, length = 200)
	private String idempotencyKey;

	@Column(name = "task_type", nullable = false, length = 128)
	private String taskType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 64)
	private TaskStatus status;

	@Column(nullable = false)
	private int attempts;

	@Column(name = "max_attempts", nullable = false)
	private int maxAttempts;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "started_at")
	private Instant startedAt;

	@Column(name = "completed_at")
	private Instant completedAt;

	@Column(name = "next_run_at")
	private Instant nextRunAt;

	@Column(name = "last_error_code", length = 128)
	private String lastErrorCode;

	@Column(name = "last_error_message", length = 1024)
	private String lastErrorMessage;

	@Column(name = "correlation_id", length = 128)
	private String correlationId;

	@Column(name = "input_summary", columnDefinition = "TEXT")
	private String inputSummary;

	@Column(name = "result_summary", columnDefinition = "TEXT")
	private String resultSummary;

	@Version
	private long version;

	protected TaskRun() {
	}

	private TaskRun(
		UUID id,
		String idempotencyKey,
		String taskType,
		int maxAttempts,
		String correlationId,
		String inputSummary,
		Instant now) {
		this.id = id;
		this.idempotencyKey = idempotencyKey;
		this.taskType = taskType;
		this.status = TaskStatus.QUEUED;
		this.attempts = 0;
		this.maxAttempts = maxAttempts;
		this.createdAt = now;
		this.updatedAt = now;
		this.correlationId = correlationId;
		this.inputSummary = inputSummary;
	}

	static TaskRun queued(
		String idempotencyKey,
		String taskType,
		int maxAttempts,
		String correlationId,
		String inputSummary,
		Clock clock) {
		return new TaskRun(UUID.randomUUID(), idempotencyKey, taskType, maxAttempts, correlationId, inputSummary, clock.instant());
	}

	void markRunning(Clock clock) {
		Instant now = clock.instant();
		this.status = TaskStatus.RUNNING;
		this.attempts += 1;
		this.startedAt = now;
		this.updatedAt = now;
		this.nextRunAt = null;
	}

	void markSucceeded(String resultSummary, Clock clock) {
		Instant now = clock.instant();
		this.status = TaskStatus.SUCCEEDED;
		this.resultSummary = resultSummary;
		this.completedAt = now;
		this.updatedAt = now;
		this.nextRunAt = null;
		this.lastErrorCode = null;
		this.lastErrorMessage = null;
	}

	void markFailed(String errorCode, String errorMessage, Clock clock) {
		Instant now = clock.instant();
		this.lastErrorCode = errorCode;
		this.lastErrorMessage = errorMessage;
		this.updatedAt = now;
		if (attempts < maxAttempts) {
			this.status = TaskStatus.RETRY_SCHEDULED;
			this.nextRunAt = now.plus(Duration.ofMinutes(Math.max(1, attempts)));
		}
		else {
			this.status = TaskStatus.FAILED;
			this.completedAt = now;
			this.nextRunAt = null;
		}
	}

	public UUID id() {
		return id;
	}

	public String idempotencyKey() {
		return idempotencyKey;
	}

	public String taskType() {
		return taskType;
	}

	public TaskStatus status() {
		return status;
	}

	public int attempts() {
		return attempts;
	}

	public int maxAttempts() {
		return maxAttempts;
	}

	public Instant createdAt() {
		return createdAt;
	}

	public Instant updatedAt() {
		return updatedAt;
	}

	public Instant startedAt() {
		return startedAt;
	}

	public Instant completedAt() {
		return completedAt;
	}

	public Instant nextRunAt() {
		return nextRunAt;
	}

	public String lastErrorCode() {
		return lastErrorCode;
	}

	public String lastErrorMessage() {
		return lastErrorMessage;
	}

	public String correlationId() {
		return correlationId;
	}

	public String inputSummary() {
		return inputSummary;
	}

	public String resultSummary() {
		return resultSummary;
	}
}
