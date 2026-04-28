package com.clm.platform.domain.certificate;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "certificate_key_references")
public class CertificateKeyReference {

	@Id
	private UUID id;

	@Column(name = "certificate_id", nullable = false)
	private UUID certificateId;

	@Column(name = "certificate_version_id", nullable = false)
	private UUID certificateVersionId;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Column(name = "provider_type", nullable = false, length = 64)
	private String providerType;

	@Column(name = "reference_uri", nullable = false, length = 512)
	private String referenceUri;

	@Column(name = "key_alias", length = 128)
	private String keyAlias;

	@Column(name = "key_algorithm", nullable = false, length = 32)
	private String keyAlgorithm;

	@Column(name = "key_match_verified", nullable = false)
	private boolean keyMatchVerified;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "created_by", length = 256)
	private String createdBy;

	protected CertificateKeyReference() {
	}

	private CertificateKeyReference(
			UUID id,
			UUID certificateId,
			UUID certificateVersionId,
			UUID tenantId,
			NormalizedKeyReference keyReference,
			String keyAlgorithm,
			Instant now,
			String createdBy) {
		this.id = id;
		this.certificateId = certificateId;
		this.certificateVersionId = certificateVersionId;
		this.tenantId = tenantId;
		this.providerType = keyReference.providerType();
		this.referenceUri = keyReference.referenceUri();
		this.keyAlias = keyReference.keyAlias();
		this.keyAlgorithm = keyAlgorithm;
		this.keyMatchVerified = true;
		this.createdAt = now;
		this.createdBy = createdBy;
	}

	static CertificateKeyReference create(
			ManagedCertificate certificate,
			CertificateVersion version,
			NormalizedKeyReference keyReference,
			String keyAlgorithm,
			Instant now,
			String createdBy) {
		return new CertificateKeyReference(
			UUID.randomUUID(),
			certificate.id(),
			version.id(),
			certificate.tenantId(),
			keyReference,
			keyAlgorithm,
			now,
			createdBy);
	}

	boolean sameReference(NormalizedKeyReference keyReference, String keyAlgorithm) {
		return Objects.equals(providerType, keyReference.providerType())
			&& Objects.equals(referenceUri, keyReference.referenceUri())
			&& Objects.equals(keyAlias, keyReference.keyAlias())
			&& Objects.equals(this.keyAlgorithm, keyAlgorithm)
			&& keyMatchVerified;
	}

	public UUID id() {
		return id;
	}

	public UUID certificateId() {
		return certificateId;
	}

	public UUID certificateVersionId() {
		return certificateVersionId;
	}

	public UUID tenantId() {
		return tenantId;
	}

	public String providerType() {
		return providerType;
	}

	public String referenceUri() {
		return referenceUri;
	}

	public String keyAlias() {
		return keyAlias;
	}

	public String keyAlgorithm() {
		return keyAlgorithm;
	}

	public boolean keyMatchVerified() {
		return keyMatchVerified;
	}

	public Instant createdAt() {
		return createdAt;
	}

	public String createdBy() {
		return createdBy;
	}
}
