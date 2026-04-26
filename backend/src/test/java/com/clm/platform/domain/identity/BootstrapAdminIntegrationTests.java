package com.clm.platform.domain.identity;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.clm.platform.testsupport.CleanDatabase;
import com.clm.platform.testsupport.TestBootstrap;

@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	properties = { "debug=false", "logging.level.root=INFO", "logging.level.org.springframework=INFO" })
@ActiveProfiles("test")
@CleanDatabase
class BootstrapAdminIntegrationTests {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void freshDeploymentAllowsBootstrapOnceAndThenDisablesIt() throws Exception {
		ResponseEntity<String> initialStatus = restTemplate.getForEntity(url("/api/v1/bootstrap/status"), String.class);
		assertThat(initialStatus.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(objectMapper.readTree(initialStatus.getBody()).path("required").asBoolean()).isTrue();

		BootstrapAdminResponse bootstrap = TestBootstrap.createAdmin(restTemplate, baseUrl());
		assertThat(bootstrap.email()).isEqualTo(TestBootstrap.ADMIN_EMAIL);
		assertThat(bootstrap.tenantId()).isNotNull();
		assertThat(bootstrap.organizationId()).isNotNull();

		ResponseEntity<String> finalStatus = restTemplate.getForEntity(url("/api/v1/bootstrap/status"), String.class);
		assertThat(finalStatus.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(objectMapper.readTree(finalStatus.getBody()).path("required").asBoolean()).isFalse();

		ResponseEntity<String> secondBootstrap = restTemplate.postForEntity(
			url("/api/v1/bootstrap/admin"),
			TestBootstrap.adminRequest(),
			String.class);
		assertThat(secondBootstrap.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(objectMapper.readTree(secondBootstrap.getBody()).path("code").asText()).isEqualTo("CONFLICT");
	}

	@Test
	void bootstrappedAdminCanUseBasicAuthForProtectedApis() throws Exception {
		TestBootstrap.createAdmin(restTemplate, baseUrl());

		ResponseEntity<String> response = TestBootstrap.adminClient(restTemplate)
			.getForEntity(url("/api/v1/tenants"), String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		JsonNode body = objectMapper.readTree(response.getBody());
		assertThat(body).hasSize(1);
		assertThat(body.get(0).path("slug").asText()).isEqualTo("default");
	}

	private String baseUrl() {
		return "http://localhost:" + port;
	}

	private String url(String path) {
		return baseUrl() + path;
	}
}
