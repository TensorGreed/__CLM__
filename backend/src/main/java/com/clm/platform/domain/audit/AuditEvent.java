package com.clm.platform.domain.audit;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_events")
public class AuditEvent {

	@Id
	private UUID id;

	@Column(name = "occurred_at", nullable = false)
	private Instant occurredAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "actor_type", nullable = false, length = 64)
	private AuditActorType actorType;

	@Column(name = "actor_id", length = 256)
	private String actorId;

	@Column(name = "tenant_id", length = 128)
	private String tenantId;

	@Column(nullable = false, length = 128)
	private String action;

	@Column(name = "resource_type", nullable = false, length = 128)
	private String resourceType;

	@Column(name = "resource_id", length = 256)
	private String resourceId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 64)
	private AuditDecision decision;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 64)
	private AuditStatus status;

	@Column(length = 1024)
	private String reason;

	@Column(name = "correlation_id", length = 128)
	private String correlationId;

	@Column(columnDefinition = "TEXT")
	private String metadata;

	protected AuditEvent() {
	}

	AuditEvent(
		UUID id,
		Instant occurredAt,
		AuditActorType actorType,
		String actorId,
		String tenantId,
		String action,
		String resourceType,
		String resourceId,
		AuditDecision decision,
		AuditStatus status,
		String reason,
		String correlationId,
		String metadata) {
		this.id = id;
		this.occurredAt = occurredAt;
		this.actorType = actorType;
		this.actorId = actorId;
		this.tenantId = tenantId;
		this.action = action;
		this.resourceType = resourceType;
		this.resourceId = resourceId;
		this.decision = decision;
		this.status = status;
		this.reason = reason;
		this.correlationId = correlationId;
		this.metadata = metadata;
	}

	public UUID id() {
		return id;
	}

	public Instant occurredAt() {
		return occurredAt;
	}

	public AuditActorType actorType() {
		return actorType;
	}

	public String actorId() {
		return actorId;
	}

	public String tenantId() {
		return tenantId;
	}

	public String action() {
		return action;
	}

	public String resourceType() {
		return resourceType;
	}

	public String resourceId() {
		return resourceId;
	}

	public AuditDecision decision() {
		return decision;
	}

	public AuditStatus status() {
		return status;
	}

	public String reason() {
		return reason;
	}

	public String correlationId() {
		return correlationId;
	}

	public String metadata() {
		return metadata;
	}
}
