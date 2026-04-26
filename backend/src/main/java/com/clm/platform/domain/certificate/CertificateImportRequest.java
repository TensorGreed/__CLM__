package com.clm.platform.domain.certificate;

import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CertificateImportRequest(
	@NotNull UUID tenantId,
	@NotBlank @Size(max = 20000) String certificatePem,
	@Size(max = 100000) String chainPem,
	@Size(max = 256) String owner,
	boolean orphaned,
	@Size(max = 32) Set<@NotBlank @Size(max = 64) String> tags) {

	@AssertTrue(message = "Either owner must be provided or orphaned must be true.")
	public boolean hasOwnerOrOrphanedState() {
		return owner != null && !owner.isBlank() || orphaned;
	}
}
