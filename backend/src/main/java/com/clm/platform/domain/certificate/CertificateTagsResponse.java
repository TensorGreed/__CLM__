package com.clm.platform.domain.certificate;

import java.util.Set;
import java.util.UUID;

public record CertificateTagsResponse(UUID certificateId, Set<String> tags) {

	static CertificateTagsResponse from(ManagedCertificate certificate) {
		return new CertificateTagsResponse(certificate.id(), certificate.tags());
	}
}
