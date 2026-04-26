package com.clm.platform.api.error;

import java.time.Instant;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.clm.platform.observability.CorrelationIds;
import com.clm.platform.security.SensitiveDataRedactor;

@RestControllerAdvice
public class ApiExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

	private final SensitiveDataRedactor redactor;

	public ApiExceptionHandler(SensitiveDataRedactor redactor) {
		this.redactor = redactor;
	}

	@ExceptionHandler(ApiException.class)
	ResponseEntity<ApiErrorResponse> handleApiException(ApiException exception, HttpServletRequest request) {
		return error(exception.errorCode(), exception.getMessage(), request, List.of());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
		List<FieldErrorResponse> details = exception.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(this::fieldError)
			.toList();

		return error(ApiErrorCode.VALIDATION_FAILED, "Request validation failed.", request, details);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException exception, HttpServletRequest request) {
		List<FieldErrorResponse> details = exception.getConstraintViolations()
			.stream()
			.map(violation -> new FieldErrorResponse(
				violation.getPropertyPath().toString(),
				redactor.redact(violation.getMessage())))
			.toList();

		return error(ApiErrorCode.VALIDATION_FAILED, "Request validation failed.", request, details);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ResponseEntity<ApiErrorResponse> handleUnreadableRequest(HttpMessageNotReadableException exception, HttpServletRequest request) {
		return error(ApiErrorCode.MALFORMED_REQUEST, "Request body is malformed.", request, List.of());
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	ResponseEntity<ApiErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException exception, HttpServletRequest request) {
		return error(ApiErrorCode.METHOD_NOT_ALLOWED, "HTTP method is not supported for this resource.", request, List.of());
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
		LOGGER.error("Unhandled API exception correlationId={}", CorrelationIds.current(), exception);
		return error(ApiErrorCode.INTERNAL_ERROR, "An unexpected error occurred.", request, List.of());
	}

	private FieldErrorResponse fieldError(FieldError fieldError) {
		return new FieldErrorResponse(fieldError.getField(), redactor.redact(fieldError.getDefaultMessage()));
	}

	private ResponseEntity<ApiErrorResponse> error(
		ApiErrorCode code,
		String message,
		HttpServletRequest request,
		List<FieldErrorResponse> details) {

		ApiErrorResponse response = new ApiErrorResponse(
			code.code(),
			redactor.redact(message),
			code.remediation(),
			CorrelationIds.current(),
			request.getRequestURI(),
			Instant.now(),
			details);

		return ResponseEntity.status(code.status()).body(response);
	}
}
