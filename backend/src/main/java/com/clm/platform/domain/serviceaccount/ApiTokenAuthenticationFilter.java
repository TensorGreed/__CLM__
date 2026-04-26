package com.clm.platform.domain.serviceaccount;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ApiTokenAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";

	private final ApiTokenAuthenticator apiTokenAuthenticator;

	public ApiTokenAuthenticationFilter(ApiTokenAuthenticator apiTokenAuthenticator) {
		this.apiTokenAuthenticator = apiTokenAuthenticator;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String authorizationHeader = request.getHeader("Authorization");
		if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
			String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
			apiTokenAuthenticator.authenticate(token)
				.ifPresent(authentication -> SecurityContextHolder.getContext().setAuthentication(authentication));
		}
		filterChain.doFilter(request, response);
	}
}
