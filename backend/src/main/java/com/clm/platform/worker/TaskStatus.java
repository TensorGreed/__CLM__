package com.clm.platform.worker;

public enum TaskStatus {

	QUEUED,
	RUNNING,
	RETRY_SCHEDULED,
	SUCCEEDED,
	FAILED,
	CANCELED;

	public boolean isRunnable() {
		return this == QUEUED || this == RETRY_SCHEDULED;
	}
}
