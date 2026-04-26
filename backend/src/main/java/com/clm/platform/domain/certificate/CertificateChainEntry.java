package com.clm.platform.domain.certificate;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "certificate_chain_entries")
public class CertificateChainEntry {

	@Id
	private UUID id;

	@Column(name = "certificate_version_id", nullable = false)
	private UUID certificateVersionId;

	@Column(nullable = false)
	private int position;

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

	@Column(name = "self_signed", nullable = false)
	private boolean selfSigned;

	protected CertificateChainEntry() {
	}

	private CertificateChainEntry(UUID id, UUID certificateVersionId, int position, ParsedChainCertificate parsed) {
		this.id = id;
		this.certificateVersionId = certificateVersionId;
		this.position = position;
		this.subjectDn = parsed.subjectDn();
		this.issuerDn = parsed.issuerDn();
		this.serialNumber = parsed.serialNumber();
		this.notBefore = parsed.notBefore();
		this.notAfter = parsed.notAfter();
		this.sha256Fingerprint = parsed.sha256Fingerprint();
		this.sha1Fingerprint = parsed.sha1Fingerprint();
		this.selfSigned = parsed.selfSigned();
	}

	public static CertificateChainEntry from(UUID certificateVersionId, int position, ParsedChainCertificate parsed) {
		return new CertificateChainEntry(UUID.randomUUID(), certificateVersionId, position, parsed);
	}

	public int position() {
		return position;
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

	public boolean selfSigned() {
		return selfSigned;
	}
}
