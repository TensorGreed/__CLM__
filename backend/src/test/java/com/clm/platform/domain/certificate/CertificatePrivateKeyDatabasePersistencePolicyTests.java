package com.clm.platform.domain.certificate;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.clm.platform.domain.identity.BootstrapAdminResponse;
import com.clm.platform.testsupport.CleanDatabase;
import com.clm.platform.testsupport.TestBootstrap;

@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	properties = {
		"debug=false",
		"logging.level.root=INFO",
		"logging.level.org.springframework=INFO",
		"clm.key-handling.private-key-import-enabled=true",
		"clm.key-handling.storage-provider=external-reference",
		"clm.key-handling.database-persistence-enabled=true" })
@ActiveProfiles("test")
@CleanDatabase
class CertificatePrivateKeyDatabasePersistencePolicyTests {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ManagedCertificateRepository certificateRepository;

	@Autowired
	private CertificateKeyReferenceRepository keyReferenceRepository;

	@Test
	void rejectsDatabasePrivateKeyPersistenceEvenWhenImportIsEnabled() throws Exception {
		BootstrapAdminResponse bootstrap = TestBootstrap.createAdmin(restTemplate, baseUrl());

		ResponseEntity<String> response = TestBootstrap.adminClient(restTemplate)
			.postForEntity(
				url("/api/v1/certificates/import-with-private-key"),
				new CertificatePrivateKeyImportRequest(
					bootstrap.tenantId(),
					CertificatePemFixtures.MATCHING_RSA_CERTIFICATE,
					null,
					CertificatePemFixtures.MATCHING_RSA_PRIVATE_KEY,
					new CertificateKeyReferenceRequest("vault", "vault://pki/team/web", null),
					"external-reference",
					"platform-team",
					false,
					Set.of()),
				String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		assertThat(objectMapper.readTree(response.getBody()).path("message").asText())
			.isEqualTo("Database private key persistence is disabled in this build.");
		assertThat(certificateRepository.count()).isZero();
		assertThat(keyReferenceRepository.count()).isZero();
	}

	private String baseUrl() {
		return "http://localhost:" + port;
	}

	private String url(String path) {
		return baseUrl() + path;
	}
}
