package com.clm.platform.domain.audit;

import java.time.Clock;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.clm.platform.observability.CorrelationIds;
import com.clm.platform.security.SensitiveDataRedactor;

@Service
@Validated
public class AuditEventService {

	private final AuditEventRepository auditEventRepository;

	private final Clock clock;

	private final SensitiveDataRedactor redactor;

	public AuditEventService(AuditEventRepository auditEventRepository, Clock clock, SensitiveDataRedactor redactor) {
		this.auditEventRepository = auditEventRepository;
		this.clock = clock;
		this.redactor = redactor;
	}

	@Transactional
	public AuditEvent append(@Valid AuditEventCommand command) {
		String correlationId = command.correlationId() == null ? CorrelationIds.current() : command.correlationId();
		AuditEvent event = new AuditEvent(
			UUID.randomUUID(),
			clock.instant(),
			command.actorType(),
			command.actorId(),
			command.tenantId(),
			command.action(),
			command.resourceType(),
			command.resourceId(),
			command.decision(),
			command.status(),
			redactor.redact(command.reason()),
			correlationId,
			redactor.redact(command.metadata()));

		return auditEventRepository.save(event);
	}
}
