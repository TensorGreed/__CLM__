package com.clm.platform.api.query;

import java.util.List;

import com.clm.platform.api.error.ApiErrorCode;
import com.clm.platform.api.error.ApiException;

public record FilterSpec(String field, String value) {

	public static List<FilterSpec> parse(List<String> rawFilters) {
		if (rawFilters == null) {
			return List.of();
		}

		return rawFilters.stream().map(FilterSpec::parseOne).toList();
	}

	private static FilterSpec parseOne(String rawFilter) {
		if (rawFilter == null || rawFilter.isBlank()) {
			throw new ApiException(ApiErrorCode.VALIDATION_FAILED, "Filter cannot be blank.");
		}

		String[] parts = rawFilter.split(":", 2);
		if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
			throw new ApiException(ApiErrorCode.VALIDATION_FAILED, "Filter must use field:value syntax.");
		}

		return new FilterSpec(parts[0].trim(), parts[1].trim());
	}
}
