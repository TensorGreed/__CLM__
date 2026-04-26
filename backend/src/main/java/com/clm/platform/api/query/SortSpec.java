package com.clm.platform.api.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.clm.platform.api.error.ApiErrorCode;
import com.clm.platform.api.error.ApiException;

public record SortSpec(String field, SortDirection direction) {

	public static List<SortSpec> parse(List<String> rawSorts) {
		if (rawSorts == null) {
			return List.of();
		}

		List<SortSpec> sorts = new ArrayList<>();
		for (int index = 0; index < rawSorts.size(); index++) {
			String rawSort = rawSorts.get(index);
			if (rawSort != null && !rawSort.contains(",") && index + 1 < rawSorts.size() && isDirection(rawSorts.get(index + 1))) {
				sorts.add(parseOne(rawSort + "," + rawSorts.get(index + 1)));
				index++;
			}
			else {
				sorts.add(parseOne(rawSort));
			}
		}
		return sorts;
	}

	private static SortSpec parseOne(String rawSort) {
		if (rawSort == null || rawSort.isBlank()) {
			throw new ApiException(ApiErrorCode.VALIDATION_FAILED, "Sort cannot be blank.");
		}

		String[] parts = rawSort.split(",", 2);
		String field = parts[0].trim();
		if (field.isBlank()) {
			throw new ApiException(ApiErrorCode.VALIDATION_FAILED, "Sort field cannot be blank.");
		}

		SortDirection direction = parts.length == 1
			? SortDirection.ASC
			: parseDirection(parts[1]);

		return new SortSpec(field, direction);
	}

	private static SortDirection parseDirection(String rawDirection) {
		try {
			return SortDirection.valueOf(rawDirection.trim().toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException exception) {
			throw new ApiException(ApiErrorCode.VALIDATION_FAILED, "Sort direction must be asc or desc.");
		}
	}

	private static boolean isDirection(String rawDirection) {
		if (rawDirection == null) {
			return false;
		}
		String normalized = rawDirection.trim().toUpperCase(Locale.ROOT);
		return "ASC".equals(normalized) || "DESC".equals(normalized);
	}
}
