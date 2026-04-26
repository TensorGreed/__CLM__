package com.clm.platform.domain.certificate;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record CertificateVersionResponse(
	UUID id,
	int versionNumber,
	CertificateVersionSource source,
	String subject,
	String issuer,
	String serialNumber,
	Instant validFrom,
	Instant validTo,
	String sha256Fingerprint,
	String sha1Fingerprint,
	String publicKeyAlgorithm,
	String signatureAlgorithm,
	Set<String> subjectAlternativeNames,
	int chainLength,
	boolean selfSigned,
	Instant createdAt) {

	static CertificateVersionResponse from(CertificateVersion version) {
		return new CertificateVersionResponse(
			version.id(),
			version.versionNumber(),
			version.source(),
			version.subjectDn(),
			version.issuerDn(),
			version.serialNumber(),
			version.notBefore(),
			version.notAfter(),
			version.sha256Fingerprint(),
			version.sha1Fingerprint(),
			version.publicKeyAlgorithm(),
			version.signatureAlgorithm(),
			version.subjectAlternativeNames(),
			version.chainLength(),
			version.selfSigned(),
			version.createdAt());
	}
}
