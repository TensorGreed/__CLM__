package com.clm.platform.domain.certificate;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.clm.platform.api.error.ApiErrorCode;
import com.clm.platform.api.error.ApiException;

final class PemSafetyInspector {

	private static final Pattern PEM_BEGIN_PATTERN = Pattern.compile(
		"-----BEGIN\\s+([A-Z0-9][A-Z0-9 ._-]*?)\\s*-----",
		Pattern.CASE_INSENSITIVE);

	private PemSafetyInspector() {
	}

	static List<PemBlockSummary> summarize(String pem) {
		if (pem == null || pem.isBlank()) {
			return List.of();
		}

		Matcher matcher = PEM_BEGIN_PATTERN.matcher(pem);
		return matcher.results()
			.map(match -> normalizedLabel(match.group(1)))
			.map(label -> new PemBlockSummary(label, isPrivateKeyLabel(label)))
			.toList();
	}

	static void rejectPrivateKeyMaterial(String pem, String fieldName) {
		if (containsPrivateKeyMaterial(pem)) {
			throw validation(fieldName + " must not contain private key material.");
		}
	}

	static PemBlockSummary requirePrivateKeyMaterial(String pem, String fieldName) {
		if (pem == null || pem.isBlank()) {
			throw validation(fieldName + " is required.");
		}

		List<PemBlockSummary> blocks = summarize(pem);
		return blocks.stream()
			.filter(PemBlockSummary::privateKey)
			.findFirst()
			.orElseThrow(() -> validation(fieldName + " must contain a private key PEM block."));
	}

	static boolean containsPrivateKeyMaterial(String pem) {
		return summarize(pem).stream().anyMatch(PemBlockSummary::privateKey);
	}

	private static String normalizedLabel(String label) {
		return label.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
	}

	private static boolean isPrivateKeyLabel(String label) {
		return label.contains("PRIVATE KEY");
	}

	private static ApiException validation(String message) {
		return new ApiException(ApiErrorCode.VALIDATION_FAILED, message);
	}
}
