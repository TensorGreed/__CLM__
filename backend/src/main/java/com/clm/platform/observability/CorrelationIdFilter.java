package com.clm.platform.observability;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

	private static final int MAX_CORRELATION_ID_LENGTH = 128;

	private final String headerName;

	public CorrelationIdFilter(@Value("${clm.logging.correlation-header:X-Correlation-ID}") String headerName) {
		this.headerName = headerName;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String correlationId = resolveCorrelationId(request);
		request.setAttribute(CorrelationIds.REQUEST_ATTRIBUTE, correlationId);
		response.setHeader(headerName, correlationId);
		MDC.put(CorrelationIds.MDC_KEY, correlationId);

		try {
			filterChain.doFilter(request, response);
		}
		finally {
			MDC.remove(CorrelationIds.MDC_KEY);
		}
	}

	private String resolveCorrelationId(HttpServletRequest request) {
		String headerValue = request.getHeader(headerName);
		if (headerValue == null || headerValue.isBlank()) {
			return UUID.randomUUID().toString();
		}

		String trimmed = headerValue.trim();
		if (trimmed.length() > MAX_CORRELATION_ID_LENGTH || !trimmed.matches("[A-Za-z0-9._:-]+")) {
			return UUID.randomUUID().toString();
		}

		return trimmed;
	}
}
