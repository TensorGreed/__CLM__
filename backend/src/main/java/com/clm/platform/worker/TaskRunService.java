package com.clm.platform.worker;

import java.time.Clock;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.clm.platform.api.error.ResourceNotFoundException;
import com.clm.platform.observability.CorrelationIds;
import com.clm.platform.security.SensitiveDataRedactor;

@Service
@Validated
public class TaskRunService {

	private final TaskRunRepository taskRunRepository;

	private final Clock clock;

	private final SensitiveDataRedactor redactor;

	public TaskRunService(TaskRunRepository taskRunRepository, Clock clock, SensitiveDataRedactor redactor) {
		this.taskRunRepository = taskRunRepository;
		this.clock = clock;
		this.redactor = redactor;
	}

	@Transactional
	public TaskRun createQueued(@Valid TaskRunRequest request) {
		return taskRunRepository.findByIdempotencyKey(request.idempotencyKey())
			.orElseGet(() -> createNew(request));
	}

	@Transactional(readOnly = true)
	public TaskRun get(UUID taskId) {
		return taskRunRepository.findById(taskId)
			.orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
	}

	@Transactional
	public TaskRun markRunning(UUID taskId) {
		TaskRun taskRun = get(taskId);
		taskRun.markRunning(clock);
		return taskRun;
	}

	@Transactional
	public TaskRun markSucceeded(UUID taskId, String resultSummary) {
		TaskRun taskRun = get(taskId);
		taskRun.markSucceeded(redactor.redact(resultSummary), clock);
		return taskRun;
	}

	@Transactional
	public TaskRun markFailed(UUID taskId, String errorCode, String errorMessage) {
		TaskRun taskRun = get(taskId);
		taskRun.markFailed(errorCode, redactor.redact(errorMessage), clock);
		return taskRun;
	}

	private TaskRun createNew(TaskRunRequest request) {
		try {
			TaskRun taskRun = TaskRun.queued(
				request.idempotencyKey(),
				request.taskType(),
				request.resolvedMaxAttempts(),
				CorrelationIds.current(),
				redactor.redact(request.inputSummary()),
				clock);
			return taskRunRepository.save(taskRun);
		}
		catch (DataIntegrityViolationException exception) {
			return taskRunRepository.findByIdempotencyKey(request.idempotencyKey()).orElseThrow(() -> exception);
		}
	}
}
