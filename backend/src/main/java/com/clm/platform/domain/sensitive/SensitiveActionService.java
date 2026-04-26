package com.clm.platform.domain.sensitive;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clm.platform.domain.audit.AuditDecision;
import com.clm.platform.domain.audit.AuditEvent;
import com.clm.platform.domain.audit.AuditEventCommand;
import com.clm.platform.domain.audit.AuditEventService;
import com.clm.platform.domain.audit.AuditStatus;
import com.clm.platform.security.CurrentActor;

@Service
public class SensitiveActionService {

	private final AuditEventService auditEventService;

	public SensitiveActionService(AuditEventService auditEventService) {
		this.auditEventService = auditEventService;
	}

	@Transactional
	public SensitiveActionResponse capture(SensitiveActionRequest request) {
		AuditEvent event = auditEventService.append(new AuditEventCommand(
			CurrentActor.actorType(),
			CurrentActor.actorId(),
			null,
			"sensitive." + request.actionType().name().toLowerCase(),
			request.resourceType(),
			request.resourceId(),
			AuditDecision.ALLOW,
			AuditStatus.SUCCESS,
			request.reason(),
			null,
			null));

		return new SensitiveActionResponse(event.id(), request.actionType(), request.resourceType(), request.resourceId());
	}
}
