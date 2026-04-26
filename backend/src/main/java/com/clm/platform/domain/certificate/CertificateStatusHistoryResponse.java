package com.clm.platform.domain.certificate;

import java.time.Instant;
import java.util.UUID;

public record CertificateStatusHistoryResponse(
	UUID id,
	CertificateStatus fromStatus,
	CertificateStatus toStatus,
	String reason,
	String changedBy,
	Instant changedAt) {

	static CertificateStatusHistoryResponse from(CertificateStatusHistory history) {
		return new CertificateStatusHistoryResponse(
			history.id(),
			history.fromStatus(),
			history.toStatus(),
			history.reason(),
			history.changedBy(),
			history.changedAt());
	}
}
