package com.clm.platform.api.error;

public class ConflictException extends ApiException {

	public ConflictException(String message) {
		super(ApiErrorCode.CONFLICT, message);
	}
}
