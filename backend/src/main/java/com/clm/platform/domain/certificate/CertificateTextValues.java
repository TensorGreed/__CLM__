package com.clm.platform.domain.certificate;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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

	static String packMap(Map<String, String> values) {
		if (values == null || values.isEmpty()) {
			return "";
		}
		Map<String, String> sorted = new TreeMap<>(values);
		StringBuilder packed = new StringBuilder();
		for (var entry : sorted.entrySet()) {
			if (!packed.isEmpty()) {
				packed.append('\n');
			}
			packed.append(entry.getKey()).append('=').append(entry.getValue());
		}
		return packed.toString();
	}

	static Map<String, String> unpackMap(String packed) {
		if (packed == null || packed.isBlank()) {
			return Map.of();
		}
		Map<String, String> values = new LinkedHashMap<>();
		for (String line : packed.split("\\R")) {
			int separatorIndex = line.indexOf('=');
			if (separatorIndex > 0) {
				values.put(line.substring(0, separatorIndex), line.substring(separatorIndex + 1));
			}
		}
		return values;
	}
}
