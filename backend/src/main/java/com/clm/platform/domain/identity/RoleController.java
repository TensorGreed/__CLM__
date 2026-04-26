package com.clm.platform.domain.identity;

import java.util.Arrays;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clm.platform.api.ApiPaths;
import com.clm.platform.security.BuiltInRole;
import com.clm.platform.security.Permission;

@RestController
public class RoleController {

	@GetMapping(ApiPaths.API_V1 + "/roles")
	@PreAuthorize("hasAuthority('PERMISSION_ROLE_READ')")
	List<RoleResponse> roles() {
		return Arrays.stream(BuiltInRole.values()).map(RoleResponse::from).toList();
	}

	@GetMapping(ApiPaths.API_V1 + "/permissions")
	@PreAuthorize("hasAuthority('PERMISSION_ROLE_READ')")
	List<PermissionResponse> permissions() {
		return Arrays.stream(Permission.values()).map(permission -> new PermissionResponse(permission.name())).toList();
	}
}
