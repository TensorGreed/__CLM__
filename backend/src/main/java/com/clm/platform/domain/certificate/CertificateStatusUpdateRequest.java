package com.clm.platform.domain.certificate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CertificateStatusUpdateRequest(
	@NotNull CertificateStatus status,
	@NotBlank @Size(max = 1024) String reason) {
}
