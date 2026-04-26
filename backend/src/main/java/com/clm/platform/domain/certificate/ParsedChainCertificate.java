package com.clm.platform.domain.certificate;

import java.time.Instant;

public record ParsedChainCertificate(
	String subjectDn,
	String issuerDn,
	String serialNumber,
	Instant notBefore,
	Instant notAfter,
	String sha256Fingerprint,
	String sha1Fingerprint,
	boolean selfSigned) {
}
