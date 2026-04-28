package com.clm.platform.domain.certificate;

import java.time.Instant;
import java.util.UUID;

public record CertificateKeyReferenceResponse(
	UUID id,
	UUID certificateId,
	UUID certificateVersionId,
	UUID tenantId,
	String providerType,
	String referenceUri,
	String keyAlias,
	String keyAlgorithm,
	boolean keyMatchVerified,
	Instant createdAt,
	String createdBy) {

	static CertificateKeyReferenceResponse from(CertificateKeyReference keyReference) {
		if (keyReference == null) {
			return null;
		}
		return new CertificateKeyReferenceResponse(
			keyReference.id(),
			keyReference.certificateId(),
			keyReference.certificateVersionId(),
			keyReference.tenantId(),
			keyReference.providerType(),
			keyReference.referenceUri(),
			keyReference.keyAlias(),
			keyReference.keyAlgorithm(),
			keyReference.keyMatchVerified(),
			keyReference.createdAt(),
			keyReference.createdBy());
	}
}
