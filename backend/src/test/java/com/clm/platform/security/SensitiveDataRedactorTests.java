package com.clm.platform.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SensitiveDataRedactorTests {

	private final SensitiveDataRedactor redactor = new SensitiveDataRedactor();

	@Test
	void redactsAssignmentStyleSecretsAndPrivateKeyBlocks() {
		String redacted = redactor.redact("""
			password=clear-text
			token:raw-token
			-----BEGIN PRIVATE KEY-----
			abc-secret-key-body
			-----END PRIVATE KEY-----
			""");

		assertThat(redacted).contains("password=[REDACTED]");
		assertThat(redacted).contains("token=[REDACTED]");
		assertThat(redacted).contains("[REDACTED_PRIVATE_KEY]");
		assertThat(redacted).doesNotContain("clear-text", "raw-token", "abc-secret-key-body");
	}
}
