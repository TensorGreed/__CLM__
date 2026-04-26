package com.clm.platform.domain.identity;

import java.util.List;

import com.clm.platform.security.BuiltInRole;

public record RoleResponse(
	String key,
	List<String> permissions) {

	static RoleResponse from(BuiltInRole role) {
		return new RoleResponse(
			role.name(),
			role.permissions().stream().map(Enum::name).sorted().toList());
	}
}
