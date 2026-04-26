package com.clm.platform.domain.certificate;

import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CertificateMetadataUpdateRequest(
	@Size(max = 50) Map<@NotBlank @Size(max = 64) String, @Valid CertificateMetadataValueRequest> metadata) {
}
