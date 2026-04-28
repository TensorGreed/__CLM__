package com.clm.platform.domain.certificate;

import java.util.UUID;

public record CertificateImportResponse(
	UUID certificateId,
	UUID versionId,
	UUID tenantId,
	boolean imported,
	CertificateStatus status,
	String sha256Fingerprint,
	CertificateKeyReferenceResponse keyReference) {

	static CertificateImportResponse from(ManagedCertificate certificate, CertificateVersion version, boolean imported) {
		return from(certificate, version, imported, null);
	}

	static CertificateImportResponse from(
			ManagedCertificate certificate,
			CertificateVersion version,
			boolean imported,
			CertificateKeyReference keyReference) {
		return new CertificateImportResponse(
			certificate.id(),
			version.id(),
			certificate.tenantId(),
			imported,
			certificate.status(),
			version.sha256Fingerprint(),
			CertificateKeyReferenceResponse.from(keyReference));
	}
}
