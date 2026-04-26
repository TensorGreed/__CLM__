package com.clm.platform.worker;

public interface TaskHandler {

	String taskType();

	TaskResult handle(TaskRunContext context);
}
