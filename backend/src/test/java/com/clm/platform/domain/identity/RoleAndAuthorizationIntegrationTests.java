package com.clm.platform.domain.identity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import com.clm.platform.security.BuiltInRole;
import com.clm.platform.testsupport.CleanDatabase;
import com.clm.platform.testsupport.TestBootstrap;

@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	properties = { "debug=false", "logging.level.root=INFO", "logging.level.org.springframework=INFO" })
@ActiveProfiles("test")
@CleanDatabase
class RoleAndAuthorizationIntegrationTests {

	private static final String READ_ONLY_EMAIL = "readonly@example.test";

	private static final String READ_ONLY_PASSWORD = "read-only-password";

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserAccountRepository userAccountRepository;

	@Autowired
	private UserRoleAssignmentRepository roleAssignmentRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private Clock clock;

	@Test
	void protectedApisReturnJsonUnauthenticatedError() throws Exception {
		ResponseEntity<String> response = restTemplate.getForEntity(url("/api/v1/tenants"), String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		JsonNode body = objectMapper.readTree(response.getBody());
		assertThat(body.path("code").asText()).isEqualTo("UNAUTHENTICATED");
		assertThat(body.path("correlationId").asText()).isNotBlank();
	}

	@Test
	void builtInRoleMatrixIsAvailableToAdministrators() throws Exception {
		TestBootstrap.createAdmin(restTemplate, baseUrl());

		ResponseEntity<String> response = TestBootstrap.adminClient(restTemplate)
			.getForEntity(url("/api/v1/roles"), String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		JsonNode body = objectMapper.readTree(response.getBody());
		assertThat(body.toString()).contains("ADMIN", "OPERATOR", "REQUESTER", "APPROVER", "AUDITOR", "READ_ONLY", "SERVICE_ACCOUNT");
		assertThat(body.toString()).contains("TENANT_MANAGE", "SERVICE_ACCOUNT_MANAGE");
	}

	@Test
	void tenantScopedReadOnlyUserCanReadButCannotCreateTenant() throws Exception {
		BootstrapAdminResponse bootstrap = TestBootstrap.createAdmin(restTemplate, baseUrl());
		UserAccount readOnlyUser = userAccountRepository.save(new UserAccount(
			UUID.randomUUID(),
			READ_ONLY_EMAIL,
			"Read Only User",
			passwordEncoder.encode(READ_ONLY_PASSWORD),
			clock.instant()));
		roleAssignmentRepository.save(new UserRoleAssignment(
			UUID.randomUUID(),
			readOnlyUser.id(),
			bootstrap.tenantId(),
			null,
			BuiltInRole.READ_ONLY,
			clock.instant()));

		TestRestTemplate readOnlyClient = restTemplate.withBasicAuth(READ_ONLY_EMAIL, READ_ONLY_PASSWORD);
		ResponseEntity<String> listResponse = readOnlyClient.getForEntity(url("/api/v1/tenants"), String.class);
		assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(objectMapper.readTree(listResponse.getBody())).hasSize(1);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		ResponseEntity<String> createResponse = readOnlyClient.postForEntity(
			url("/api/v1/tenants"),
			new HttpEntity<>("{\"slug\":\"other\",\"name\":\"Other Tenant\"}", headers),
			String.class);
		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		assertThat(objectMapper.readTree(createResponse.getBody()).path("code").asText()).isEqualTo("FORBIDDEN");
	}

	private String baseUrl() {
		return "http://localhost:" + port;
	}

	private String url(String path) {
		return baseUrl() + path;
	}
}
