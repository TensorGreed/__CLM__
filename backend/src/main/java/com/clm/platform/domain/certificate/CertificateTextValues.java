package com.clm.platform.domain.certificate;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

final class CertificateTextValues {

	private CertificateTextValues() {
	}

	static String pack(Collection<String> values) {
		if (values == null || values.isEmpty()) {
			return "";
		}
		return String.join("\n", values);
	}

	static Set<String> unpackToSet(String packed) {
		if (packed == null || packed.isBlank()) {
			return Set.of();
		}
		Set<String> values = new LinkedHashSet<>();
		for (String value : packed.split("\\R")) {
			if (!value.isBlank()) {
				values.add(value);
			}
		}
		return values;
	}
}
