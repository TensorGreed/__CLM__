package com.clm.platform.api.error;

import org.springframework.http.HttpStatus;

public enum ApiErrorCode {

	VALIDATION_FAILED(
		"VALIDATION_FAILED",
		HttpStatus.BAD_REQUEST,
		"Fix the invalid request fields and retry."),
	MALFORMED_REQUEST(
		"MALFORMED_REQUEST",
		HttpStatus.BAD_REQUEST,
		"Check the request syntax and content type."),
	RESOURCE_NOT_FOUND(
		"RESOURCE_NOT_FOUND",
		HttpStatus.NOT_FOUND,
		"Verify the resource identifier and tenant scope."),
	METHOD_NOT_ALLOWED(
		"METHOD_NOT_ALLOWED",
		HttpStatus.METHOD_NOT_ALLOWED,
		"Use one of the supported HTTP methods for this resource."),
	INTERNAL_ERROR(
		"INTERNAL_ERROR",
		HttpStatus.INTERNAL_SERVER_ERROR,
		"Retry later or contact the platform operator with the correlation ID.");

	private final String code;

	private final HttpStatus status;

	private final String remediation;

	ApiErrorCode(String code, HttpStatus status, String remediation) {
		this.code = code;
		this.status = status;
		this.remediation = remediation;
	}

	public String code() {
		return code;
	}

	public HttpStatus status() {
		return status;
	}

	public String remediation() {
		return remediation;
	}
}
