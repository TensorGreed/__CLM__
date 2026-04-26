package com.clm.platform.worker;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class TaskRunner {

	private final TaskRunService taskRunService;

	private final Map<String, TaskHandler> handlers;

	public TaskRunner(TaskRunService taskRunService, List<TaskHandler> handlers) {
		this.taskRunService = taskRunService;
		this.handlers = handlers.stream().collect(Collectors.toMap(TaskHandler::taskType, Function.identity()));
	}

	public TaskRun run(UUID taskId) {
		TaskRun currentTask = taskRunService.get(taskId);
		if (!currentTask.status().isRunnable()) {
			return currentTask;
		}

		TaskRun runningTask = taskRunService.markRunning(taskId);
		TaskHandler handler = handlers.get(runningTask.taskType());
		if (handler == null) {
			return taskRunService.markFailed(taskId, "TASK_HANDLER_NOT_FOUND", "No task handler is registered.");
		}

		TaskResult result = handler.handle(new TaskRunContext(
			runningTask.id(),
			runningTask.taskType(),
			runningTask.idempotencyKey(),
			runningTask.correlationId(),
			runningTask.inputSummary()));

		if (result.success()) {
			return taskRunService.markSucceeded(taskId, result.summary());
		}

		return taskRunService.markFailed(taskId, result.errorCode(), result.errorMessage());
	}
}
