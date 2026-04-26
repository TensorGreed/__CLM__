package com.clm.platform.domain.certificate;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CertificateTagsUpdateRequest(
	@Size(max = 32) Set<@NotBlank @Size(max = 64) String> tags) {
}
