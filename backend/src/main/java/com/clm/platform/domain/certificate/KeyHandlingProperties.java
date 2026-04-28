package com.clm.platform.domain.certificate;

import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "clm.key-handling")
public record KeyHandlingProperties(
	boolean privateKeyImportEnabled,
	String storageProvider,
	boolean databasePersistenceEnabled) {

	private static final Pattern STORAGE_PROVIDER_PATTERN = Pattern.compile("[a-z0-9._:-]{1,64}");

	public KeyHandlingProperties {
		if (storageProvider == null || storageProvider.isBlank()) {
			storageProvider = "disabled";
		}
	}

	String normalizedStorageProvider() {
		String normalized = storageProvider.trim().toLowerCase(Locale.ROOT);
		return STORAGE_PROVIDER_PATTERN.matcher(normalized).matches() ? normalized : "invalid";
	}
}
