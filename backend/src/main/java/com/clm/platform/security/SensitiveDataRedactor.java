package com.clm.platform.security;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class SensitiveDataRedactor {

	private static final Pattern SENSITIVE_ASSIGNMENT = Pattern.compile(
		"(?i)(password|passwd|pwd|secret|token|api[_-]?key|private[_ -]?key)\\s*[:=]\\s*[^\\s,;]+");

	private static final Pattern PRIVATE_KEY_BLOCK = Pattern.compile(
		"-----BEGIN\\s+[A-Z0-9 ._-]*PRIVATE KEY\\s*-----[\\s\\S]*?-----END\\s+[A-Z0-9 ._-]*PRIVATE KEY\\s*-----",
		Pattern.CASE_INSENSITIVE);

	public String redact(String value) {
		if (value == null) {
			return null;
		}

		String withoutPrivateKeys = PRIVATE_KEY_BLOCK.matcher(value).replaceAll("[REDACTED_PRIVATE_KEY]");
		return SENSITIVE_ASSIGNMENT.matcher(withoutPrivateKeys).replaceAll("$1=[REDACTED]");
	}
}
