package com.clm.platform.domain.certificate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CertificateKeyReferenceRequest(
	@NotBlank @Size(max = 64) String providerType,
	@NotBlank @Size(max = 512) String referenceUri,
	@Size(max = 128) String keyAlias) {
}
