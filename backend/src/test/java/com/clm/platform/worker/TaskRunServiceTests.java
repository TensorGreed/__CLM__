package com.clm.platform.worker;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = { "debug=false", "logging.level.root=INFO", "logging.level.org.springframework=INFO" })
@ActiveProfiles("test")
class TaskRunServiceTests {

	@Autowired
	private TaskRunService taskRunService;

	@Autowired
	private TaskRunRepository taskRunRepository;

	@Test
	void createQueuedIsIdempotentByKey() {
		TaskRunRequest request = new TaskRunRequest("idem-1", "test.task", 3, "password=secret");

		TaskRun first = taskRunService.createQueued(request);
		TaskRun second = taskRunService.createQueued(request);

		assertThat(second.id()).isEqualTo(first.id());
		assertThat(taskRunRepository.findAll()).extracting(TaskRun::idempotencyKey).contains("idem-1");
		assertThat(first.inputSummary()).contains("password=[REDACTED]");
	}

	@Test
	void failedTaskMovesToRetryUntilAttemptsAreExhausted() {
		TaskRun taskRun = taskRunService.createQueued(new TaskRunRequest("idem-2", "test.task", 2, "input"));

		taskRunService.markRunning(taskRun.id());
		TaskRun failed = taskRunService.markFailed(taskRun.id(), "TEST_FAILURE", "token=abc");

		assertThat(failed.status()).isEqualTo(TaskStatus.RETRY_SCHEDULED);
		assertThat(failed.nextRunAt()).isNotNull();
		assertThat(failed.lastErrorMessage()).contains("token=[REDACTED]");
	}

	@Test
	void runnerUsesRegisteredHandlerAndDoesNotRerunTerminalTask() {
		TaskRun taskRun = taskRunService.createQueued(new TaskRunRequest("idem-3", "test.success", 3, "input"));
		TaskRunner runner = new TaskRunner(taskRunService, List.of(new TaskHandler() {
			@Override
			public String taskType() {
				return "test.success";
			}

			@Override
			public TaskResult handle(TaskRunContext context) {
				return TaskResult.success("done");
			}
		}));

		TaskRun succeeded = runner.run(taskRun.id());
		TaskRun secondRun = runner.run(taskRun.id());

		assertThat(succeeded.status()).isEqualTo(TaskStatus.SUCCEEDED);
		assertThat(secondRun.attempts()).isEqualTo(1);
		assertThat(secondRun.resultSummary()).isEqualTo("done");
	}
}
