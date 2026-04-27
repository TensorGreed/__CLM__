package com.clm.platform.domain.search;

import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.clm.platform.api.ApiPaths;

@RestController
@Validated
public class SearchController {

	private final SearchService searchService;

	public SearchController(SearchService searchService) {
		this.searchService = searchService;
	}

	@GetMapping(ApiPaths.API_V1 + "/search")
	@PreAuthorize("hasAuthority('PERMISSION_CERTIFICATE_READ')")
	SearchResponse search(
			@RequestParam("q") @Size(max = 200) String query,
			@RequestParam(required = false) UUID tenantId,
			@RequestParam(required = false, defaultValue = "10") @Min(1) @Max(25) int limit) {
		return searchService.search(query, tenantId, limit);
	}
}
