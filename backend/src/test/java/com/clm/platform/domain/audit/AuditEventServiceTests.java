package com.clm.platform.domain.audit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = { "debug=false", "logging.level.root=INFO", "logging.level.org.springframework=INFO" })
@ActiveProfiles("test")
class AuditEventServiceTests {

	@Autowired
	private AuditEventService auditEventService;

	@Autowired
	private AuditEventRepository auditEventRepository;

	@Test
	void appendsRedactedAuditEvent() {
		AuditEvent event = auditEventService.append(new AuditEventCommand(
			AuditActorType.SYSTEM,
			"bootstrap",
			"tenant-a",
			"platform.test",
			"platform",
			"root",
			AuditDecision.ALLOW,
			AuditStatus.SUCCESS,
			"password=secret should be redacted",
			"correlation-a",
			"token=abc"));

		AuditEvent persisted = auditEventRepository.findById(event.id()).orElseThrow();
		assertThat(persisted.reason()).contains("password=[REDACTED]");
		assertThat(persisted.metadata()).contains("token=[REDACTED]");
		assertThat(persisted.correlationId()).isEqualTo("correlation-a");
	}
}
