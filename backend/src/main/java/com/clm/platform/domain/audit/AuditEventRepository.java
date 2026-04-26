package com.clm.platform.domain.audit;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {

	List<AuditEvent> findTop25ByResourceTypeAndResourceIdOrderByOccurredAtDesc(String resourceType, String resourceId);
}
