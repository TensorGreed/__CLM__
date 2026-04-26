package com.clm.platform.domain.serviceaccount;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Component;

@Component
public class TokenGenerator {

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	public GeneratedToken generate() {
		byte[] tokenBytes = new byte[32];
		SECURE_RANDOM.nextBytes(tokenBytes);
		String secret = "clm_" + Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
		return new GeneratedToken(secret.substring(0, 12), secret);
	}

	public record GeneratedToken(String prefix, String secret) {
	}
}
