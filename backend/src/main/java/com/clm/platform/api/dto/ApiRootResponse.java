package com.clm.platform.api.dto;

import java.time.Instant;
import java.util.List;

public record ApiRootResponse(
	String version,
	String name,
	Instant serverTime,
	List<LinkResponse> links) {
}
