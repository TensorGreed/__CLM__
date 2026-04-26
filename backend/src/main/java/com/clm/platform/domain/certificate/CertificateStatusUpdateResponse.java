package com.clm.platform.domain.certificate;

import java.util.UUID;

public record CertificateStatusUpdateResponse(
	UUID certificateId,
	CertificateStatus status,
	boolean changed,
	CertificateStatusHistoryResponse transition) {
}
