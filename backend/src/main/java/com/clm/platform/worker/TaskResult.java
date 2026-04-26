package com.clm.platform.worker;

public record TaskResult(boolean success, String summary, String errorCode, String errorMessage) {

	public static TaskResult success(String summary) {
		return new TaskResult(true, summary, null, null);
	}

	public static TaskResult failure(String errorCode, String errorMessage) {
		return new TaskResult(false, null, errorCode, errorMessage);
	}
}
