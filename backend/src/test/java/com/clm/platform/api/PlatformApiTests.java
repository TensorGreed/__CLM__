package com.clm.platform.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

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

import com.clm.platform.testsupport.CleanDatabase;
import com.clm.platform.testsupport.TestBootstrap;

@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	properties = { "debug=false", "logging.level.root=INFO", "logging.level.org.springframework=INFO" })
@ActiveProfiles("test")
@CleanDatabase
class PlatformApiTests {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void apiRootExposesVersionedFoundation() throws Exception {
		ResponseEntity<String> response = restTemplate.getForEntity(url(ApiPaths.API_V1), String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		JsonNode body = objectMapper.readTree(response.getBody());
		assertThat(body.path("version").asText()).isEqualTo("v1");
		assertThat(body.path("links").toString()).contains("/v3/api-docs");
	}

	@Test
	void openApiDocumentIsGenerated() {
		ResponseEntity<String> response = restTemplate.getForEntity(url("/v3/api-docs"), String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).contains("CLM Platform API");
	}

	@Test
	void errorEnvelopeIncludesStableCodeAndCorrelationId() throws Exception {
		TestBootstrap.createAdmin(restTemplate, url(""));
		String correlationId = "test-correlation-1";
		RequestEntity<Void> request = RequestEntity.get(url("/api/v1/tasks/" + UUID.randomUUID()))
			.header("X-Correlation-ID", correlationId)
			.header(HttpHeaders.AUTHORIZATION, basicAuthHeader())
			.build();

		ResponseEntity<String> response = restTemplate.exchange(request, String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getHeaders().getFirst("X-Correlation-ID")).isEqualTo(correlationId);

		JsonNode body = objectMapper.readTree(response.getBody());
		assertThat(body.path("code").asText()).isEqualTo("RESOURCE_NOT_FOUND");
		assertThat(body.path("correlationId").asText()).isEqualTo(correlationId);
		assertThat(body.path("remediation").asText()).isNotBlank();
	}

	@Test
	void generatedCorrelationIdIsReturnedWhenHeaderIsMissing() {
		ResponseEntity<String> response = restTemplate.getForEntity(url(ApiPaths.API_V1), String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getHeaders().getFirst("X-Correlation-ID")).isNotBlank();
	}

	private String url(String path) {
		return "http://localhost:" + port + path;
	}

	private String basicAuthHeader() {
		String credentials = TestBootstrap.ADMIN_EMAIL + ":" + TestBootstrap.ADMIN_PASSWORD;
		return "Basic " + java.util.Base64.getEncoder().encodeToString(credentials.getBytes(java.nio.charset.StandardCharsets.UTF_8));
	}
}
