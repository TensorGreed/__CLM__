package com.clm.platform.domain.search;

import java.util.List;
import java.util.UUID;

public record SearchResultResponse(
	String type,
	UUID id,
	UUID tenantId,
	String title,
	String subtitle,
	String status,
	String href,
	List<String> matchedFields) {
}
