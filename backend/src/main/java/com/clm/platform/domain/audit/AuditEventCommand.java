package com.clm.platform.domain.audit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AuditEventCommand(
	@NotNull AuditActorType actorType,
	@Size(max = 256) String actorId,
	@Size(max = 128) String tenantId,
	@NotBlank @Size(max = 128) String action,
	@NotBlank @Size(max = 128) String resourceType,
	@Size(max = 256) String resourceId,
	@NotNull AuditDecision decision,
	@NotNull AuditStatus status,
	@Size(max = 1024) String reason,
	@Size(max = 128) String correlationId,
	String metadata) {
}
