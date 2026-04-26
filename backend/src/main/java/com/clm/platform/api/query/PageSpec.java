package com.clm.platform.api.query;

import com.clm.platform.api.error.ApiErrorCode;
import com.clm.platform.api.error.ApiException;

public record PageSpec(int page, int size) {

	public static final int DEFAULT_PAGE = 0;

	public static final int DEFAULT_SIZE = 25;

	public static final int MAX_SIZE = 100;

	public static PageSpec from(Integer page, Integer size) {
		int resolvedPage = page == null ? DEFAULT_PAGE : page;
		int resolvedSize = size == null ? DEFAULT_SIZE : size;

		if (resolvedPage < 0) {
			throw new ApiException(ApiErrorCode.VALIDATION_FAILED, "Page must be greater than or equal to zero.");
		}

		if (resolvedSize < 1 || resolvedSize > MAX_SIZE) {
			throw new ApiException(ApiErrorCode.VALIDATION_FAILED, "Page size must be between 1 and " + MAX_SIZE + ".");
		}

		return new PageSpec(resolvedPage, resolvedSize);
	}
}
