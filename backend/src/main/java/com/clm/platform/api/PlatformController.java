package com.clm.platform.api;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clm.platform.api.dto.ApiRootResponse;
import com.clm.platform.api.dto.LinkResponse;

@RestController
public class PlatformController {

	private final String apiVersion;

	public PlatformController(@Value("${clm.api.version:v1}") String apiVersion) {
		this.apiVersion = apiVersion;
	}

	@GetMapping(ApiPaths.API_V1)
	ApiRootResponse root() {
		return new ApiRootResponse(
			apiVersion,
			"CLM Platform API",
			Instant.now(),
			List.of(
				new LinkResponse("self", ApiPaths.API_V1),
				new LinkResponse("openapi", "/v3/api-docs"),
				new LinkResponse("tasks", ApiPaths.API_V1 + "/tasks/{taskId}")));
	}
}
