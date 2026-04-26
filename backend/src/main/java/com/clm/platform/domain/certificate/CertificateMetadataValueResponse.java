package com.clm.platform.domain.certificate;

public record CertificateMetadataValueResponse(CertificateMetadataValueType type, String value) {

	static CertificateMetadataValueResponse from(CertificateMetadataEntry entry) {
		return new CertificateMetadataValueResponse(entry.type(), entry.value());
	}
}
