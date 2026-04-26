package com.clm.platform.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	properties = { "debug=false", "logging.level.root=INFO", "logging.level.org.springframework=INFO" })
@ActiveProfiles("test")
class SecurityConfigurationTests {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void healthEndpointIsPublic() {
		ResponseEntity<String> response = restTemplate.getForEntity(url("/actuator/health"), String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).contains("\"status\":\"UP\"");
	}

	@Test
	void nonHealthEndpointsAreClosedUntilAuthIsImplemented() {
		ResponseEntity<String> response = restTemplate.getForEntity(url("/api/v1"), String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	private String url(String path) {
		return "http://localhost:" + port + path;
	}
}
