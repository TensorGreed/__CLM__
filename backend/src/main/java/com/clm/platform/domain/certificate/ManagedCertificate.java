package com.clm.platform.domain.certificate;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "certificates")
public class ManagedCertificate {

	@Id
	private UUID id;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Column(length = 256)
	private String owner;

	@Column(nullable = false)
	private boolean orphaned;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private CertificateStatus status;

	@Column(name = "current_version_id")
	private UUID currentVersionId;

	@Column(name = "common_name", length = 256)
	private String commonName;

	@Column(name = "subject_dn", nullable = false, columnDefinition = "TEXT")
	private String subjectDn;

	@Column(name = "issuer_dn", nullable = false, columnDefinition = "TEXT")
	private String issuerDn;

	@Column(name = "serial_number", nullable = false, length = 128)
	private String serialNumber;

	@Column(name = "not_before", nullable = false)
	private Instant notBefore;

	@Column(name = "not_after", nullable = false)
	private Instant notAfter;

	@Column(name = "sha256_fingerprint", nullable = false, length = 95)
	private String sha256Fingerprint;

	@Column(name = "sha1_fingerprint", nullable = false, length = 59)
	private String sha1Fingerprint;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String sans;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@ElementCollection
	@CollectionTable(name = "certificate_tags", joinColumns = @JoinColumn(name = "certificate_id"))
	@Column(name = "tag", nullable = false, length = 64)
	private Set<String> tags = new HashSet<>();

	protected ManagedCertificate() {
	}

	private ManagedCertificate(
			UUID id,
			UUID tenantId,
			String owner,
			boolean orphaned,
			CertificateStatus status,
			ParsedCertificate parsedCertificate,
			Set<String> tags,
			Instant now) {
		this.id = id;
		this.tenantId = tenantId;
		this.owner = owner;
		this.orphaned = orphaned;
		this.status = status;
		this.commonName = parsedCertificate.commonName();
		this.subjectDn = parsedCertificate.subjectDn();
		this.issuerDn = parsedCertificate.issuerDn();
		this.serialNumber = parsedCertificate.serialNumber();
		this.notBefore = parsedCertificate.notBefore();
		this.notAfter = parsedCertificate.notAfter();
		this.sha256Fingerprint = parsedCertificate.sha256Fingerprint();
		this.sha1Fingerprint = parsedCertificate.sha1Fingerprint();
		this.sans = CertificateTextValues.pack(parsedCertificate.subjectAlternativeNames());
		this.tags.addAll(tags);
		this.createdAt = now;
		this.updatedAt = now;
	}

	public static ManagedCertificate create(
			UUID tenantId,
			String owner,
			boolean orphaned,
			CertificateStatus status,
			ParsedCertificate parsedCertificate,
			Set<String> tags,
			Instant now) {
		return new ManagedCertificate(UUID.randomUUID(), tenantId, owner, orphaned, status, parsedCertificate, tags, now);
	}

	public void assignCurrentVersion(UUID versionId, Instant now) {
		this.currentVersionId = versionId;
		this.updatedAt = now;
	}

	public void replaceTags(Set<String> tags, Instant now) {
		this.tags.clear();
		this.tags.addAll(tags);
		this.updatedAt = now;
	}

	public void transitionStatus(CertificateStatus status, Instant now) {
		this.status = status;
		this.updatedAt = now;
	}

	public void touch(Instant now) {
		this.updatedAt = now;
	}

	public UUID id() {
		return id;
	}

	public UUID tenantId() {
		return tenantId;
	}

	public String owner() {
		return owner;
	}

	public boolean orphaned() {
		return orphaned;
	}

	public CertificateStatus status() {
		return status;
	}

	public UUID currentVersionId() {
		return currentVersionId;
	}

	public String commonName() {
		return commonName;
	}

	public String subjectDn() {
		return subjectDn;
	}

	public String issuerDn() {
		return issuerDn;
	}

	public String serialNumber() {
		return serialNumber;
	}

	public Instant notBefore() {
		return notBefore;
	}

	public Instant notAfter() {
		return notAfter;
	}

	public String sha256Fingerprint() {
		return sha256Fingerprint;
	}

	public String sha1Fingerprint() {
		return sha1Fingerprint;
	}

	public Set<String> subjectAlternativeNames() {
		return CertificateTextValues.unpackToSet(sans);
	}

	public Set<String> tags() {
		return Set.copyOf(tags);
	}

	public Instant createdAt() {
		return createdAt;
	}

	public Instant updatedAt() {
		return updatedAt;
	}
}
