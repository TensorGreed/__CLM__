package com.clm.platform.domain.certificate;

import java.time.Instant;
import java.util.List;

public record ParsedCertificate(
	String normalizedCertificatePem,
	String subjectDn,
	String commonName,
	String issuerDn,
	String serialNumber,
	Instant notBefore,
	Instant notAfter,
	String sha256Fingerprint,
	String sha1Fingerprint,
	String publicKeyAlgorithm,
	Integer publicKeySizeBits,
	String signatureAlgorithm,
	List<String> subjectAlternativeNames,
	boolean selfSigned,
	List<ParsedChainCertificate> chain) {

	public int chainLength() {
		return 1 + chain.size();
	}
}
