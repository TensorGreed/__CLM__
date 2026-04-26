package com.clm.platform.domain.certificate;

import java.util.Map;
import java.util.UUID;

public record CertificateMetadataResponse(
	UUID certificateId,
	Map<String, CertificateMetadataValueResponse> metadata) {
}
