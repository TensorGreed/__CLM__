package com.clm.platform.domain.search;

import java.util.List;

public record SearchResponse(
	String query,
	int limit,
	List<SearchResultResponse> results) {
}
