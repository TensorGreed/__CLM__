package com.clm.platform.domain.certificate;

import java.time.Instant;
import java.util.UUID;

import com.clm.platform.domain.audit.AuditActorType;
import com.clm.platform.domain.audit.AuditDecision;
import com.clm.platform.domain.audit.AuditEvent;
import com.clm.platform.domain.audit.AuditStatus;

public record CertificateAuditEventResponse(
	UUID id,
	Instant occurredAt,
	AuditActorType actorType,
	String actorId,
	String action,
	AuditDecision decision,
	AuditStatus status,
	String reason,
	String correlationId) {

	static CertificateAuditEventResponse from(AuditEvent event) {
		return new CertificateAuditEventResponse(
			event.id(),
			event.occurredAt(),
			event.actorType(),
			event.actorId(),
			event.action(),
			event.decision(),
			event.status(),
			event.reason(),
			event.correlationId());
	}
}
