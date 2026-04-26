package com.clm.platform.security;

import java.util.Collection;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class ServiceAccountPrincipal implements UserDetails {

	private final UUID serviceAccountId;

	private final UUID tenantId;

	private final String name;

	private final Collection<? extends GrantedAuthority> authorities;

	public ServiceAccountPrincipal(
		UUID serviceAccountId,
		UUID tenantId,
		String name,
		Collection<? extends GrantedAuthority> authorities) {
		this.serviceAccountId = serviceAccountId;
		this.tenantId = tenantId;
		this.name = name;
		this.authorities = authorities;
	}

	public UUID serviceAccountId() {
		return serviceAccountId;
	}

	public UUID tenantId() {
		return tenantId;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return "";
	}

	@Override
	public String getUsername() {
		return name;
	}
}
