package com.clm.platform.domain.serviceaccount;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.clm.platform.domain.identity.BootstrapAdminResponse;
import com.clm.platform.security.Permission;
import com.clm.platform.testsupport.CleanDatabase;
import com.clm.platform.testsupport.TestBootstrap;

@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	properties = { "debug=false", "logging.level.root=INFO", "logging.level.org.springframework=INFO" })
@ActiveProfiles("test")
@CleanDatabase
class ServiceAccountTokenIntegrationTests {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void serviceAccountTokensCanBeCreatedRotatedScopedAndRevoked() throws Exception {
		BootstrapAdminResponse bootstrap = TestBootstrap.createAdmin(restTemplate, baseUrl());
		TestRestTemplate adminClient = TestBootstrap.adminClient(restTemplate);

		ResponseEntity<ServiceAccountResponse> serviceAccountResponse = adminClient.postForEntity(
			url("/api/v1/service-accounts"),
			new ServiceAccountCreateRequest(bootstrap.tenantId(), "inventory-importer"),
			ServiceAccountResponse.class);
		assertThat(serviceAccountResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(serviceAccountResponse.getBody()).isNotNull();

		ResponseEntity<ApiTokenSecretResponse> tokenResponse = adminClient.postForEntity(
			url("/api/v1/service-accounts/" + serviceAccountResponse.getBody().id() + "/tokens"),
			new ApiTokenCreateRequest(Set.of(Permission.TENANT_READ), Instant.now().plusSeconds(3600)),
			ApiTokenSecretResponse.class);
		assertThat(tokenResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(tokenResponse.getBody()).isNotNull();
		String firstToken = tokenResponse.getBody().token();
		assertThat(firstToken).startsWith("clm_");

		ResponseEntity<String> scopedRead = restTemplate.exchange(
			RequestEntity.get(url("/api/v1/tenants"))
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + firstToken)
				.build(),
			String.class);
		assertThat(scopedRead.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(objectMapper.readTree(scopedRead.getBody())).hasSize(1);

		ResponseEntity<ApiTokenSecretResponse> rotatedTokenResponse = adminClient.postForEntity(
			url("/api/v1/api-tokens/" + tokenResponse.getBody().id() + "/rotate"),
			null,
			ApiTokenSecretResponse.class);
		assertThat(rotatedTokenResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(rotatedTokenResponse.getBody()).isNotNull();
		String rotatedToken = rotatedTokenResponse.getBody().token();
		assertThat(rotatedToken).isNotEqualTo(firstToken);

		ResponseEntity<String> oldTokenRead = restTemplate.exchange(
			RequestEntity.get(url("/api/v1/tenants"))
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + firstToken)
				.build(),
			String.class);
		assertThat(oldTokenRead.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

		ResponseEntity<String> rotatedTokenRead = restTemplate.exchange(
			RequestEntity.get(url("/api/v1/tenants"))
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + rotatedToken)
				.build(),
			String.class);
		assertThat(rotatedTokenRead.getStatusCode()).isEqualTo(HttpStatus.OK);

		ResponseEntity<JsonNode> revokedTokenResponse = adminClient.postForEntity(
			url("/api/v1/api-tokens/" + tokenResponse.getBody().id() + "/revoke"),
			null,
			JsonNode.class);
		assertThat(revokedTokenResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(revokedTokenResponse.getBody().path("status").asText()).isEqualTo("REVOKED");

		ResponseEntity<String> revokedTokenRead = restTemplate.exchange(
			RequestEntity.get(url("/api/v1/tenants"))
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + rotatedToken)
				.build(),
			String.class);
		assertThat(revokedTokenRead.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	private String baseUrl() {
		return "http://localhost:" + port;
	}

	private String url(String path) {
		return baseUrl() + path;
	}
}
