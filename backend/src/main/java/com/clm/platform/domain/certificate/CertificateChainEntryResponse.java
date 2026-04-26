package com.clm.platform.domain.certificate;

import java.time.Instant;

public record CertificateChainEntryResponse(
	int position,
	String subject,
	String issuer,
	String serialNumber,
	Instant validFrom,
	Instant validTo,
	String sha256Fingerprint,
	String sha1Fingerprint,
	boolean selfSigned) {

	static CertificateChainEntryResponse from(CertificateChainEntry chainEntry) {
		return new CertificateChainEntryResponse(
			chainEntry.position(),
			chainEntry.subjectDn(),
			chainEntry.issuerDn(),
			chainEntry.serialNumber(),
			chainEntry.notBefore(),
			chainEntry.notAfter(),
			chainEntry.sha256Fingerprint(),
			chainEntry.sha1Fingerprint(),
			chainEntry.selfSigned());
	}
}
