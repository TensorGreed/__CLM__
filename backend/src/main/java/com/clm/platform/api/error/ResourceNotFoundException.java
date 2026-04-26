package com.clm.platform.api.error;

public class ResourceNotFoundException extends ApiException {

	public ResourceNotFoundException(String resourceType, Object resourceId) {
		super(
			ApiErrorCode.RESOURCE_NOT_FOUND,
			"%s '%s' was not found.".formatted(resourceType, resourceId));
	}
}
