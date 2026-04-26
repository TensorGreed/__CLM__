package com.clm.platform.domain.identity;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.clm.platform.api.ApiPaths;

@RestController
public class BootstrapController {

	private final BootstrapService bootstrapService;

	public BootstrapController(BootstrapService bootstrapService) {
		this.bootstrapService = bootstrapService;
	}

	@GetMapping(ApiPaths.API_V1 + "/bootstrap/status")
	BootstrapStatusResponse status() {
		return new BootstrapStatusResponse(bootstrapService.isBootstrapRequired());
	}

	@PostMapping(ApiPaths.API_V1 + "/bootstrap/admin")
	BootstrapAdminResponse createAdmin(@Valid @RequestBody BootstrapAdminRequest request) {
		return bootstrapService.createInitialAdmin(request);
	}
}
