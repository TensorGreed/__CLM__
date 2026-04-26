package com.clm.platform.security;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.clm.platform.api.error.ApiErrorCode;
import com.clm.platform.api.error.ApiErrorResponse;
import com.clm.platform.observability.CorrelationIds;

@Component
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper;

	public ApiAuthenticationEntryPoint(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
			throws IOException {
		ApiErrorCode code = ApiErrorCode.UNAUTHENTICATED;
		response.setStatus(code.status().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getWriter(), new ApiErrorResponse(
			code.code(),
			"Authentication is required.",
			code.remediation(),
			CorrelationIds.current(),
			request.getRequestURI(),
			Instant.now(),
			List.of()));
	}
}
