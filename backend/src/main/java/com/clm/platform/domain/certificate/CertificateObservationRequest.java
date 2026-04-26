package com.clm.platform.domain.certificate;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CertificateObservationRequest(
	@NotNull UUID tenantId,
	@NotBlank @Size(max = 20_000) String certificatePem,
	@Size(max = 100_000) String chainPem,
	@NotBlank @Size(max = 64) String sourceType,
	@NotBlank @Size(max = 256) String sourceKey,
	@NotBlank @Size(max = 512) String observedResourceKey,
	@Size(max = 256) String sourceName,
	@Size(max = 256) String owner,
	boolean orphaned,
	@Size(max = 32) Set<@NotBlank @Size(max = 64) String> tags,
	@Size(max = 32) Map<@NotBlank @Size(max = 64) String, @NotBlank @Size(max = 512) String> sourceMetadata) {

	@AssertTrue(message = "Either owner must be provided or orphaned must be true.")
	boolean hasOwnerOrIsOrphaned() {
		return owner != null && !owner.isBlank() || orphaned;
	}
}
