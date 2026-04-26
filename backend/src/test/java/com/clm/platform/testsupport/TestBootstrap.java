package com.clm.platform.testsupport;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;

import com.clm.platform.domain.identity.BootstrapAdminRequest;
import com.clm.platform.domain.identity.BootstrapAdminResponse;

public final class TestBootstrap {

	public static final String ADMIN_EMAIL = "admin@example.test";

	public static final String ADMIN_PASSWORD = "correct-horse-battery";

	private TestBootstrap() {
	}

	public static BootstrapAdminRequest adminRequest() {
		return new BootstrapAdminRequest(
			ADMIN_EMAIL,
			"Test Admin",
			ADMIN_PASSWORD,
			"default",
			"Default Tenant",
			"platform",
			"Platform Operations");
	}

	public static BootstrapAdminResponse createAdmin(TestRestTemplate restTemplate, String baseUrl) {
		var response = restTemplate.postForEntity(baseUrl + "/api/v1/bootstrap/admin", adminRequest(), BootstrapAdminResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		return response.getBody();
	}

	public static TestRestTemplate adminClient(TestRestTemplate restTemplate) {
		return restTemplate.withBasicAuth(ADMIN_EMAIL, ADMIN_PASSWORD);
	}
}
