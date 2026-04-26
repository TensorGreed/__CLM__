package com.clm.platform.api.error;

public class ForbiddenException extends ApiException {

	public ForbiddenException(String message) {
		super(ApiErrorCode.FORBIDDEN, message);
	}
}
