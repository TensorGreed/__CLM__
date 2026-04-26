package com.clm.platform.security;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.clm.platform.api.error.ApiErrorCode;
import com.clm.platform.api.error.ApiErrorResponse;
import com.clm.platform.observability.CorrelationIds;

@Component
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

	private final ObjectMapper objectMapper;

	public ApiAccessDeniedHandler(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
			throws IOException {
		ApiErrorCode code = ApiErrorCode.FORBIDDEN;
		response.setStatus(code.status().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getWriter(), new ApiErrorResponse(
			code.code(),
			"Access is denied.",
			code.remediation(),
			CorrelationIds.current(),
			request.getRequestURI(),
			Instant.now(),
			List.of()));
	}
}
