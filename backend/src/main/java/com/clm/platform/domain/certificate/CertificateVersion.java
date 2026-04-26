package com.clm.platform.domain.certificate;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "certificate_versions")
public class CertificateVersion {

	@Id
	private UUID id;

	@Column(name = "certificate_id", nullable = false)
	private UUID certificateId;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Column(name = "version_number", nullable = false)
	private int versionNumber;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private CertificateVersionSource source;

	@Column(name = "certificate_pem", nullable = false, columnDefinition = "TEXT")
	private String certificatePem;

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

	@Column(name = "public_key_algorithm", nullable = false, length = 64)
	private String publicKeyAlgorithm;

	@Column(name = "signature_algorithm", nullable = false, length = 128)
	private String signatureAlgorithm;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String sans;

	@Column(name = "chain_length", nullable = false)
	private int chainLength;

	@Column(name = "self_signed", nullable = false)
	private boolean selfSigned;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected CertificateVersion() {
	}

	private CertificateVersion(UUID id, UUID certificateId, UUID tenantId, ParsedCertificate parsedCertificate, Instant now) {
		this.id = id;
		this.certificateId = certificateId;
		this.tenantId = tenantId;
		this.versionNumber = 1;
		this.source = CertificateVersionSource.IMPORT;
		this.certificatePem = parsedCertificate.normalizedCertificatePem();
		this.subjectDn = parsedCertificate.subjectDn();
		this.issuerDn = parsedCertificate.issuerDn();
		this.serialNumber = parsedCertificate.serialNumber();
		this.notBefore = parsedCertificate.notBefore();
		this.notAfter = parsedCertificate.notAfter();
		this.sha256Fingerprint = parsedCertificate.sha256Fingerprint();
		this.sha1Fingerprint = parsedCertificate.sha1Fingerprint();
		this.publicKeyAlgorithm = parsedCertificate.publicKeyAlgorithm();
		this.signatureAlgorithm = parsedCertificate.signatureAlgorithm();
		this.sans = CertificateTextValues.pack(parsedCertificate.subjectAlternativeNames());
		this.chainLength = parsedCertificate.chainLength();
		this.selfSigned = parsedCertificate.selfSigned();
		this.createdAt = now;
	}

	public static CertificateVersion importVersion(UUID certificateId, UUID tenantId, ParsedCertificate parsedCertificate, Instant now) {
		return new CertificateVersion(UUID.randomUUID(), certificateId, tenantId, parsedCertificate, now);
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

	public int versionNumber() {
		return versionNumber;
	}

	public CertificateVersionSource source() {
		return source;
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

	public String publicKeyAlgorithm() {
		return publicKeyAlgorithm;
	}

	public String signatureAlgorithm() {
		return signatureAlgorithm;
	}

	public Set<String> subjectAlternativeNames() {
		return CertificateTextValues.unpackToSet(sans);
	}

	public int chainLength() {
		return chainLength;
	}

	public boolean selfSigned() {
		return selfSigned;
	}

	public Instant createdAt() {
		return createdAt;
	}
}
