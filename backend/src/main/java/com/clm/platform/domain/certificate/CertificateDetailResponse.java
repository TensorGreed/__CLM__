package com.clm.platform.domain.certificate;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.clm.platform.domain.audit.AuditEvent;

public record CertificateDetailResponse(
	UUID id,
	UUID tenantId,
	String owner,
	boolean orphaned,
	CertificateStatus status,
	String commonName,
	String subject,
	String issuer,
	String serialNumber,
	Instant validFrom,
	Instant validTo,
	String sha256Fingerprint,
	String sha1Fingerprint,
	Set<String> subjectAlternativeNames,
	Set<String> tags,
	CertificateVersionResponse currentVersion,
	List<CertificateVersionResponse> versions,
	List<CertificateChainEntryResponse> chain,
	List<CertificateAuditEventResponse> auditTimeline) {

	static CertificateDetailResponse from(
			ManagedCertificate certificate,
			CertificateVersion currentVersion,
			List<CertificateVersion> versions,
			List<CertificateChainEntry> chainEntries,
			List<AuditEvent> auditEvents) {
		return new CertificateDetailResponse(
			certificate.id(),
			certificate.tenantId(),
			certificate.owner(),
			certificate.orphaned(),
			certificate.status(),
			certificate.commonName(),
			certificate.subjectDn(),
			certificate.issuerDn(),
			certificate.serialNumber(),
			certificate.notBefore(),
			certificate.notAfter(),
			certificate.sha256Fingerprint(),
			certificate.sha1Fingerprint(),
			certificate.subjectAlternativeNames(),
			certificate.tags(),
			CertificateVersionResponse.from(currentVersion),
			versions.stream().map(CertificateVersionResponse::from).toList(),
			chainEntries.stream().map(CertificateChainEntryResponse::from).toList(),
			auditEvents.stream().map(CertificateAuditEventResponse::from).toList());
	}
}
