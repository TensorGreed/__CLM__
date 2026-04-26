package com.clm.platform.security;

import java.util.EnumSet;
import java.util.Set;

public enum BuiltInRole {

	ADMIN(EnumSet.allOf(Permission.class)),
	OPERATOR(EnumSet.of(
		Permission.TENANT_READ,
		Permission.ORGANIZATION_READ,
		Permission.ROLE_READ,
		Permission.TASK_READ,
		Permission.TASK_MANAGE,
		Permission.SENSITIVE_ACTION_EXECUTE)),
	REQUESTER(EnumSet.of(Permission.TENANT_READ, Permission.ORGANIZATION_READ, Permission.TASK_READ)),
	APPROVER(EnumSet.of(
		Permission.TENANT_READ,
		Permission.ORGANIZATION_READ,
		Permission.ROLE_READ,
		Permission.TASK_READ,
		Permission.AUDIT_READ,
		Permission.SENSITIVE_ACTION_EXECUTE)),
	AUDITOR(EnumSet.of(
		Permission.TENANT_READ,
		Permission.ORGANIZATION_READ,
		Permission.ROLE_READ,
		Permission.TASK_READ,
		Permission.AUDIT_READ)),
	READ_ONLY(EnumSet.of(
		Permission.TENANT_READ,
		Permission.ORGANIZATION_READ,
		Permission.ROLE_READ,
		Permission.TASK_READ)),
	SERVICE_ACCOUNT(EnumSet.of(Permission.TENANT_READ, Permission.ORGANIZATION_READ, Permission.TASK_READ));

	private final Set<Permission> permissions;

	BuiltInRole(Set<Permission> permissions) {
		this.permissions = Set.copyOf(permissions);
	}

	public Set<Permission> permissions() {
		return permissions;
	}
}
