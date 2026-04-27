package com.clm.platform.domain.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clm.platform.domain.certificate.CertificateInventoryService;
import com.clm.platform.domain.certificate.CertificateSummaryResponse;

@Service
public class SearchService {

	private final CertificateInventoryService certificateInventoryService;

	public SearchService(CertificateInventoryService certificateInventoryService) {
		this.certificateInventoryService = certificateInventoryService;
	}

	@Transactional(readOnly = true)
	public SearchResponse search(String query, UUID tenantId, int limit) {
		String normalizedQuery = query == null ? "" : query.trim();
		int normalizedLimit = Math.max(1, Math.min(limit, 25));
		if (normalizedQuery.isEmpty()) {
			return new SearchResponse("", normalizedLimit, List.of());
		}

		List<SearchResultResponse> results = certificateInventoryService
			.search(normalizedQuery, tenantId, normalizedLimit)
			.stream()
			.map(certificate -> certificateResult(certificate, normalizedQuery))
			.toList();
		return new SearchResponse(normalizedQuery, normalizedLimit, results);
	}

	private static SearchResultResponse certificateResult(CertificateSummaryResponse certificate, String query) {
		String title = certificate.commonName() != null
			? certificate.commonName()
			: certificate.subjectAlternativeNames().stream().findFirst().orElse(certificate.subject());
		String subtitle = certificate.owner() == null
			? certificate.issuer()
			: certificate.owner() + " | " + certificate.issuer();
		return new SearchResultResponse(
			"certificate",
			certificate.id(),
			certificate.tenantId(),
			title,
			subtitle,
			certificate.status().name(),
			"/certificates/" + certificate.id(),
			matchedFields(certificate, query));
	}

	private static List<String> matchedFields(CertificateSummaryResponse certificate, String query) {
		String normalizedQuery = query.toLowerCase(Locale.ROOT);
		List<String> fields = new ArrayList<>();
		if (contains(certificate.commonName(), normalizedQuery)) {
			fields.add("commonName");
		}
		if (contains(certificate.subject(), normalizedQuery)) {
			fields.add("subject");
		}
		if (contains(certificate.issuer(), normalizedQuery)) {
			fields.add("issuer");
		}
		if (contains(certificate.serialNumber(), normalizedQuery)) {
			fields.add("serialNumber");
		}
		if (contains(certificate.sha256Fingerprint(), normalizedQuery)) {
			fields.add("sha256Fingerprint");
		}
		if (contains(certificate.owner(), normalizedQuery)) {
			fields.add("owner");
		}
		if (certificate.subjectAlternativeNames().stream().anyMatch(value -> contains(value, normalizedQuery))) {
			fields.add("subjectAlternativeNames");
		}
		return fields.isEmpty() ? List.of("certificate") : fields;
	}

	private static boolean contains(String value, String normalizedQuery) {
		return value != null && value.toLowerCase(Locale.ROOT).contains(normalizedQuery);
	}
}
