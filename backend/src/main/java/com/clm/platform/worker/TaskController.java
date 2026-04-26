package com.clm.platform.worker;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.clm.platform.api.ApiPaths;

@RestController
public class TaskController {

	private final TaskRunService taskRunService;

	public TaskController(TaskRunService taskRunService) {
		this.taskRunService = taskRunService;
	}

	@GetMapping(ApiPaths.API_V1 + "/tasks/{taskId}")
	@PreAuthorize("hasAuthority('PERMISSION_TASK_READ')")
	TaskRunResponse getTask(@PathVariable UUID taskId) {
		return TaskRunResponse.from(taskRunService.get(taskId));
	}
}
