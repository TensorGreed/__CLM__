package com.clm.platform.domain.certificate;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import com.clm.platform.domain.audit.AuditDecision;
import com.clm.platform.domain.audit.AuditEventRepository;
import com.clm.platform.domain.audit.AuditStatus;
import com.clm.platform.domain.identity.BootstrapAdminResponse;
import com.clm.platform.domain.identity.UserAccountRepository;
import com.clm.platform.domain.identity.UserRoleAssignmentRepository;
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
		"clm.key-handling.database-persistence-enabled=false" })
@ActiveProfiles("test")
@CleanDatabase
class CertificatePrivateKeyImportIntegrationTests {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ManagedCertificateRepository certificateRepository;

	@Autowired
	private CertificateVersionRepository versionRepository;

	@Autowired
	private CertificateKeyReferenceRepository keyReferenceRepository;

	@Autowired
	private AuditEventRepository auditEventRepository;

	@Autowired
	@SuppressWarnings("unused")
	private UserAccountRepository userAccountRepository;

	@Autowired
	@SuppressWarnings("unused")
	private UserRoleAssignmentRepository roleAssignmentRepository;

	@Autowired
	@SuppressWarnings("unused")
	private PasswordEncoder passwordEncoder;

	@Autowired
	@SuppressWarnings("unused")
	private Clock clock;

	@Test
	void importsCertificateWithMatchingPrivateKeyAndExternalReferenceOnly() throws Exception {
		BootstrapAdminResponse bootstrap = TestBootstrap.createAdmin(restTemplate, baseUrl());

		ResponseEntity<CertificateImportResponse> response = TestBootstrap.adminClient(restTemplate)
			.postForEntity(
				url("/api/v1/certificates/import-with-private-key"),
				new CertificatePrivateKeyImportRequest(
					bootstrap.tenantId(),
					CertificatePemFixtures.MATCHING_RSA_CERTIFICATE,
					null,
					CertificatePemFixtures.MATCHING_RSA_PRIVATE_KEY,
					new CertificateKeyReferenceRequest("Vault", "vault://pki/team/web", "web-key"),
					"external-reference",
					"platform-team",
					false,
					Set.of("imported-key")),
				CertificateImportResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().imported()).isTrue();
		assertThat(response.getBody().keyReference()).isNotNull();
		assertThat(response.getBody().keyReference().providerType()).isEqualTo("vault");
		assertThat(response.getBody().keyReference().referenceUri()).isEqualTo("vault://pki/team/web");
		assertThat(response.getBody().keyReference().keyAlgorithm()).isEqualTo("RSA");
		assertThat(certificateRepository.count()).isEqualTo(1);
		assertThat(versionRepository.count()).isEqualTo(1);
		assertThat(keyReferenceRepository.count()).isEqualTo(1);

		ResponseEntity<String> detailResponse = TestBootstrap.adminClient(restTemplate)
			.getForEntity(url("/api/v1/certificates/" + response.getBody().certificateId()), String.class);
		JsonNode detail = objectMapper.readTree(detailResponse.getBody());
		assertThat(detail.path("keyReference").path("providerType").asText()).isEqualTo("vault");
		assertThat(detail.path("keyReference").path("referenceUri").asText()).isEqualTo("vault://pki/team/web");
		assertThat(detail.toString()).doesNotContain("MIIEvA", "abc-secret-key-body");

		assertThat(auditEventRepository.findAll())
			.anySatisfy(event -> {
				assertThat(event.action()).isEqualTo("certificate.private_key_match_validated");
				assertThat(event.decision()).isEqualTo(AuditDecision.ALLOW);
				assertThat(event.status()).isEqualTo(AuditStatus.SUCCESS);
				assertThat(event.metadata()).contains("\"keyAlgorithm\":\"RSA\"");
				assertThat(event.metadata()).doesNotContain("MIIEvA");
			})
			.anySatisfy(event -> {
				assertThat(event.action()).isEqualTo("certificate.key_reference_recorded");
				assertThat(event.metadata()).contains("\"providerType\":\"vault\"");
				assertThat(event.metadata()).doesNotContain("MIIEvA");
			});
	}

	@Test
	void rejectsMismatchedPrivateKeyAndMissingReferenceWithoutStoringCertificate() throws Exception {
		BootstrapAdminResponse bootstrap = TestBootstrap.createAdmin(restTemplate, baseUrl());

		ResponseEntity<String> mismatch = TestBootstrap.adminClient(restTemplate)
			.postForEntity(
				url("/api/v1/certificates/import-with-private-key"),
				new CertificatePrivateKeyImportRequest(
					bootstrap.tenantId(),
					CertificatePemFixtures.MATCHING_RSA_CERTIFICATE,
					null,
					CertificatePemFixtures.MATCHING_EC_PRIVATE_KEY,
					new CertificateKeyReferenceRequest("vault", "vault://pki/team/web", null),
					"external-reference",
					"platform-team",
					false,
					Set.of()),
				String.class);
		assertThat(mismatch.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(objectMapper.readTree(mismatch.getBody()).path("message").asText())
			.isEqualTo("Private key algorithm does not match certificate public key algorithm.");
		assertThat(certificateRepository.count()).isZero();
		assertThat(keyReferenceRepository.count()).isZero();

		ResponseEntity<String> missingReference = TestBootstrap.adminClient(restTemplate)
			.postForEntity(
				url("/api/v1/certificates/import-with-private-key"),
				new CertificatePrivateKeyImportRequest(
					bootstrap.tenantId(),
					CertificatePemFixtures.MATCHING_RSA_CERTIFICATE,
					null,
					CertificatePemFixtures.MATCHING_RSA_PRIVATE_KEY,
					null,
					"external-reference",
					"platform-team",
					false,
					Set.of()),
				String.class);
		assertThat(missingReference.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(objectMapper.readTree(missingReference.getBody()).path("message").asText())
			.isEqualTo("keyReference is required for private key import.");
		assertThat(certificateRepository.count()).isZero();
		assertThat(keyReferenceRepository.count()).isZero();

		assertThat(auditEventRepository.findAll())
			.anySatisfy(event -> {
				assertThat(event.action()).isEqualTo("certificate.private_key_match_failed");
				assertThat(event.decision()).isEqualTo(AuditDecision.DENY);
				assertThat(event.status()).isEqualTo(AuditStatus.FAILURE);
				assertThat(event.reason()).doesNotContain("MEECAQ");
			});
	}

	private String baseUrl() {
		return "http://localhost:" + port;
	}

	private String url(String path) {
		return baseUrl() + path;
	}
}
