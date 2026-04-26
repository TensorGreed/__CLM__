package com.clm.platform.domain.certificate;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "certificate_source_observations")
public class CertificateSourceObservation {

	@Id
	private UUID id;

	@Column(name = "certificate_id", nullable = false)
	private UUID certificateId;

	@Column(name = "certificate_version_id", nullable = false)
	private UUID certificateVersionId;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Column(name = "source_type", nullable = false, length = 64)
	private String sourceType;

	@Column(name = "source_key", nullable = false, length = 256)
	private String sourceKey;

	@Column(name = "observed_resource_key", nullable = false, length = 512)
	private String observedResourceKey;

	@Column(name = "source_name", length = 256)
	private String sourceName;

	@Column(name = "sha256_fingerprint", nullable = false, length = 95)
	private String sha256Fingerprint;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String metadata;

	@Column(name = "first_seen_at", nullable = false)
	private Instant firstSeenAt;

	@Column(name = "last_seen_at", nullable = false)
	private Instant lastSeenAt;

	@Column(name = "observation_count", nullable = false)
	private int observationCount;

	protected CertificateSourceObservation() {
	}

	private CertificateSourceObservation(
			UUID id,
			UUID certificateId,
			UUID certificateVersionId,
			UUID tenantId,
			String sourceType,
			String sourceKey,
			String observedResourceKey,
			String sourceName,
			String sha256Fingerprint,
			String metadata,
			Instant now) {
		this.id = id;
		this.certificateId = certificateId;
		this.certificateVersionId = certificateVersionId;
		this.tenantId = tenantId;
		this.sourceType = sourceType;
		this.sourceKey = sourceKey;
		this.observedResourceKey = observedResourceKey;
		this.sourceName = sourceName;
		this.sha256Fingerprint = sha256Fingerprint;
		this.metadata = metadata;
		this.firstSeenAt = now;
		this.lastSeenAt = now;
		this.observationCount = 1;
	}

	public static CertificateSourceObservation create(
			UUID certificateId,
			UUID certificateVersionId,
			UUID tenantId,
			String sourceType,
			String sourceKey,
			String observedResourceKey,
			String sourceName,
			String sha256Fingerprint,
			String metadata,
			Instant now) {
		return new CertificateSourceObservation(
			UUID.randomUUID(),
			certificateId,
			certificateVersionId,
			tenantId,
			sourceType,
			sourceKey,
			observedResourceKey,
			sourceName,
			sha256Fingerprint,
			metadata,
			now);
	}

	public void recordSeen(String sourceName, String metadata, Instant now) {
		this.sourceName = sourceName;
		this.metadata = metadata;
		this.lastSeenAt = now;
		this.observationCount++;
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

	public String sourceType() {
		return sourceType;
	}

	public String sourceKey() {
		return sourceKey;
	}

	public String observedResourceKey() {
		return observedResourceKey;
	}

	public String sourceName() {
		return sourceName;
	}

	public String sha256Fingerprint() {
		return sha256Fingerprint;
	}

	public String metadata() {
		return metadata;
	}

	public Instant firstSeenAt() {
		return firstSeenAt;
	}

	public Instant lastSeenAt() {
		return lastSeenAt;
	}

	public int observationCount() {
		return observationCount;
	}
}
