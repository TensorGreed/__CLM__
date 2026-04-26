package com.clm.platform.security;

public final class AuthorityNames {

	public static final String PERMISSION_PREFIX = "PERMISSION_";

	public static final String ROLE_PREFIX = "ROLE_";

	private AuthorityNames() {
	}

	public static String permission(Permission permission) {
		return PERMISSION_PREFIX + permission.name();
	}

	public static String role(BuiltInRole role) {
		return ROLE_PREFIX + role.name();
	}
}
