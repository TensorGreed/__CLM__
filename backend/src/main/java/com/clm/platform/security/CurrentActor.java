package com.clm.platform.security;

import java.util.Set;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import com.clm.platform.domain.audit.AuditActorType;

public final class CurrentActor {

	private CurrentActor() {
	}

	public static AuditActorType actorType() {
		Object principal = principal();
		if (principal instanceof ServiceAccountPrincipal) {
			return AuditActorType.SERVICE_ACCOUNT;
		}
		if (principal instanceof PlatformUserPrincipal) {
			return AuditActorType.USER;
		}
		if (principal instanceof OidcUser) {
			return AuditActorType.USER;
		}
		return AuditActorType.SYSTEM;
	}

	public static String actorId() {
		Object principal = principal();
		if (principal instanceof PlatformUserPrincipal userPrincipal) {
			return userPrincipal.userId().toString();
		}
		if (principal instanceof ServiceAccountPrincipal serviceAccountPrincipal) {
			return serviceAccountPrincipal.serviceAccountId().toString();
		}
		if (principal instanceof OidcUser oidcUser) {
			return oidcUser.getSubject();
		}
		return "system";
	}

	public static boolean hasGlobalAccess() {
		Object principal = principal();
		if (principal instanceof PlatformUserPrincipal userPrincipal) {
			return userPrincipal.globalAccess();
		}
		if (principal instanceof OidcUser) {
			return hasAuthority(AuthorityNames.role(BuiltInRole.ADMIN));
		}
		return false;
	}

	public static Set<UUID> tenantIds() {
		Object principal = principal();
		if (principal instanceof PlatformUserPrincipal userPrincipal) {
			return userPrincipal.tenantIds();
		}
		if (principal instanceof ServiceAccountPrincipal serviceAccountPrincipal) {
			return Set.of(serviceAccountPrincipal.tenantId());
		}
		return Set.of();
	}

	public static boolean hasPermission(Permission permission) {
		return hasAuthority(AuthorityNames.permission(permission));
	}

	public static boolean hasAuthority(String authorityName) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return false;
		}
		return authentication.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.anyMatch(authorityName::equals);
	}

	private static Object principal() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication == null ? null : authentication.getPrincipal();
	}
}
