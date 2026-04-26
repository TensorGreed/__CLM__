package com.clm.platform.security;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class PlatformUserPrincipal implements UserDetails {

	private final UUID userId;

	private final String email;

	private final String displayName;

	private final String password;

	private final boolean enabled;

	private final Collection<? extends GrantedAuthority> authorities;

	private final Set<UUID> tenantIds;

	private final boolean globalAccess;

	public PlatformUserPrincipal(
		UUID userId,
		String email,
		String displayName,
		String password,
		boolean enabled,
		Collection<? extends GrantedAuthority> authorities,
		Set<UUID> tenantIds,
		boolean globalAccess) {
		this.userId = userId;
		this.email = email;
		this.displayName = displayName;
		this.password = password;
		this.enabled = enabled;
		this.authorities = authorities;
		this.tenantIds = Set.copyOf(tenantIds);
		this.globalAccess = globalAccess;
	}

	public UUID userId() {
		return userId;
	}

	public String email() {
		return email;
	}

	public String displayName() {
		return displayName;
	}

	public Set<UUID> tenantIds() {
		return tenantIds;
	}

	public boolean globalAccess() {
		return globalAccess;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}
}
