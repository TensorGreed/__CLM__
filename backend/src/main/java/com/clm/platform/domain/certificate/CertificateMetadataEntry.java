package com.clm.platform.domain.certificate;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "certificate_metadata_entries")
public class CertificateMetadataEntry {

	@Id
	private UUID id;

	@Column(name = "certificate_id", nullable = false)
	private UUID certificateId;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Column(name = "metadata_key", nullable = false, length = 64)
	private String key;

	@Enumerated(EnumType.STRING)
	@Column(name = "value_type", nullable = false, length = 32)
	private CertificateMetadataValueType type;

	@Column(name = "value_text", nullable = false, columnDefinition = "TEXT")
	private String value;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "updated_by", length = 256)
	private String updatedBy;

	protected CertificateMetadataEntry() {
	}

	private CertificateMetadataEntry(
			UUID id,
			UUID certificateId,
			UUID tenantId,
			String key,
			CertificateMetadataValueType type,
			String value,
			String updatedBy,
			Instant now) {
		this.id = id;
		this.certificateId = certificateId;
		this.tenantId = tenantId;
		this.key = key;
		this.type = type;
		this.value = value;
		this.createdAt = now;
		this.updatedAt = now;
		this.updatedBy = updatedBy;
	}

	public static CertificateMetadataEntry create(
			UUID certificateId,
			UUID tenantId,
			String key,
			CertificateMetadataValueType type,
			String value,
			String updatedBy,
			Instant now) {
		return new CertificateMetadataEntry(UUID.randomUUID(), certificateId, tenantId, key, type, value, updatedBy, now);
	}

	public UUID id() {
		return id;
	}

	public UUID certificateId() {
		return certificateId;
	}

	public UUID tenantId() {
		return tenantId;
	}

	public String key() {
		return key;
	}

	public CertificateMetadataValueType type() {
		return type;
	}

	public String value() {
		return value;
	}

	public Instant createdAt() {
		return createdAt;
	}

	public Instant updatedAt() {
		return updatedAt;
	}

	public String updatedBy() {
		return updatedBy;
	}
}
