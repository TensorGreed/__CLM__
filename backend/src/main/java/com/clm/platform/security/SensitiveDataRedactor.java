package com.clm.platform.security;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class SensitiveDataRedactor {

	private static final Pattern SENSITIVE_ASSIGNMENT = Pattern.compile(
		"(?i)(password|passwd|pwd|secret|token|api[_-]?key|private[_ -]?key)\\s*[:=]\\s*[^\\s,;]+");

	public String redact(String value) {
		if (value == null) {
			return null;
		}

		return SENSITIVE_ASSIGNMENT.matcher(value).replaceAll("$1=[REDACTED]");
	}
}
