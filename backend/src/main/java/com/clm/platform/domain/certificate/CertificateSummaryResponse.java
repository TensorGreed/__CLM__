package com.clm.platform.domain.certificate;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record CertificateSummaryResponse(
	UUID id,
	UUID tenantId,
	String owner,
	boolean orphaned,
	CertificateStatus status,
	String commonName,
	String subject,
	String issuer,
	String serialNumber,
	Instant validFrom,
	Instant validTo,
	String sha256Fingerprint,
	Set<String> subjectAlternativeNames,
	Set<String> tags) {

	static CertificateSummaryResponse from(ManagedCertificate certificate) {
		return new CertificateSummaryResponse(
			certificate.id(),
			certificate.tenantId(),
			certificate.owner(),
			certificate.orphaned(),
			certificate.status(),
			certificate.commonName(),
			certificate.subjectDn(),
			certificate.issuerDn(),
			certificate.serialNumber(),
			certificate.notBefore(),
			certificate.notAfter(),
			certificate.sha256Fingerprint(),
			certificate.subjectAlternativeNames(),
			certificate.tags());
	}
}
