package com.clm.platform.api.error;

public class ApiException extends RuntimeException {

	private final ApiErrorCode errorCode;

	public ApiException(ApiErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public ApiErrorCode errorCode() {
		return errorCode;
	}
}
