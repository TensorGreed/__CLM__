package com.clm.platform.domain.certificate;

import java.util.Set;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CertificatePrivateKeyImportRequest(
	@NotNull UUID tenantId,
	@NotBlank @Size(max = 20_000) String certificatePem,
	@Size(max = 100_000) String chainPem,
	@NotBlank @Size(max = 50_000) String privateKeyPem,
	@Valid CertificateKeyReferenceRequest keyReference,
	@Size(max = 64) String keyStorageProvider,
	@Size(max = 256) String owner,
	boolean orphaned,
	@Size(max = 32) Set<@NotBlank @Size(max = 64) String> tags) {

	@AssertTrue(message = "Either owner must be provided or orphaned must be true.")
	public boolean hasOwnerOrOrphanedState() {
		return owner != null && !owner.isBlank() || orphaned;
	}
}
