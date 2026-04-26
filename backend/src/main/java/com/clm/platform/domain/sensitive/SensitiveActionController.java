package com.clm.platform.domain.sensitive;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.clm.platform.api.ApiPaths;

@RestController
public class SensitiveActionController {

	private final SensitiveActionService sensitiveActionService;

	public SensitiveActionController(SensitiveActionService sensitiveActionService) {
		this.sensitiveActionService = sensitiveActionService;
	}

	@PostMapping(ApiPaths.API_V1 + "/sensitive-actions")
	@PreAuthorize("hasAuthority('PERMISSION_SENSITIVE_ACTION_EXECUTE')")
	SensitiveActionResponse capture(@Valid @RequestBody SensitiveActionRequest request) {
		return sensitiveActionService.capture(request);
	}
}
