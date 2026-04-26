package com.clm.platform.domain.sensitive;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.clm.platform.domain.audit.AuditEventRepository;
import com.clm.platform.testsupport.CleanDatabase;
import com.clm.platform.testsupport.TestBootstrap;

@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	properties = { "debug=false", "logging.level.root=INFO", "logging.level.org.springframework=INFO" })
@ActiveProfiles("test")
@CleanDatabase
class SensitiveActionIntegrationTests {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private AuditEventRepository auditEventRepository;

	@Test
	void sensitiveActionRequiresReasonAndCreatesAuditEvent() {
		TestBootstrap.createAdmin(restTemplate, baseUrl());

		ResponseEntity<SensitiveActionResponse> response = TestBootstrap.adminClient(restTemplate)
			.postForEntity(
				url("/api/v1/sensitive-actions"),
				new SensitiveActionRequest(
					SensitiveActionType.REVOKE_CERTIFICATE,
					"certificate",
					"cert-123",
					"Revocation approved during incident response."),
				SensitiveActionResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().auditEventId()).isNotNull();
		assertThat(auditEventRepository.findById(response.getBody().auditEventId())).isPresent();
	}

	@Test
	void shortSensitiveActionReasonReturnsValidationError() throws Exception {
		TestBootstrap.createAdmin(restTemplate, baseUrl());

		ResponseEntity<String> response = TestBootstrap.adminClient(restTemplate)
			.postForEntity(
				url("/api/v1/sensitive-actions"),
				new SensitiveActionRequest(SensitiveActionType.BREAK_GLASS, "tenant", "default", "short"),
				String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(objectMapper.readTree(response.getBody()).path("code").asText()).isEqualTo("VALIDATION_FAILED");
	}

	private String baseUrl() {
		return "http://localhost:" + port;
	}

	private String url(String path) {
		return baseUrl() + path;
	}
}
