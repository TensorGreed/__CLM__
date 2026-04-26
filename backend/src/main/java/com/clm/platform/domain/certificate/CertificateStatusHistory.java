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
@Table(name = "certificate_status_history")
public class CertificateStatusHistory {

	@Id
	private UUID id;

	@Column(name = "certificate_id", nullable = false)
	private UUID certificateId;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Enumerated(EnumType.STRING)
	@Column(name = "from_status", length = 32)
	private CertificateStatus fromStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "to_status", nullable = false, length = 32)
	private CertificateStatus toStatus;

	@Column(length = 1024)
	private String reason;

	@Column(name = "changed_by", length = 256)
	private String changedBy;

	@Column(name = "changed_at", nullable = false)
	private Instant changedAt;

	protected CertificateStatusHistory() {
	}

	private CertificateStatusHistory(
			UUID id,
			UUID certificateId,
			UUID tenantId,
			CertificateStatus fromStatus,
			CertificateStatus toStatus,
			String reason,
			String changedBy,
			Instant changedAt) {
		this.id = id;
		this.certificateId = certificateId;
		this.tenantId = tenantId;
		this.fromStatus = fromStatus;
		this.toStatus = toStatus;
		this.reason = reason;
		this.changedBy = changedBy;
		this.changedAt = changedAt;
	}

	public static CertificateStatusHistory create(
			UUID certificateId,
			UUID tenantId,
			CertificateStatus fromStatus,
			CertificateStatus toStatus,
			String reason,
			String changedBy,
			Instant changedAt) {
		return new CertificateStatusHistory(
			UUID.randomUUID(),
			certificateId,
			tenantId,
			fromStatus,
			toStatus,
			reason,
			changedBy,
			changedAt);
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

	public CertificateStatus fromStatus() {
		return fromStatus;
	}

	public CertificateStatus toStatus() {
		return toStatus;
	}

	public String reason() {
		return reason;
	}

	public String changedBy() {
		return changedBy;
	}

	public Instant changedAt() {
		return changedAt;
	}
}
