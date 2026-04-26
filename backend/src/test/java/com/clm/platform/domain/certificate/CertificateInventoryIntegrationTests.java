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

import com.clm.platform.domain.identity.BootstrapAdminResponse;
import com.clm.platform.domain.identity.UserAccount;
import com.clm.platform.domain.identity.UserAccountRepository;
import com.clm.platform.domain.identity.UserRoleAssignment;
import com.clm.platform.domain.identity.UserRoleAssignmentRepository;
import com.clm.platform.domain.tenancy.TenantCreateRequest;
import com.clm.platform.domain.tenancy.TenantResponse;
import com.clm.platform.security.BuiltInRole;
import com.clm.platform.testsupport.CleanDatabase;
import com.clm.platform.testsupport.TestBootstrap;

@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	properties = { "debug=false", "logging.level.root=INFO", "logging.level.org.springframework=INFO" })
@ActiveProfiles("test")
@CleanDatabase
class CertificateInventoryIntegrationTests {

	private static final String READ_ONLY_EMAIL = "cert-reader@example.test";

	private static final String READ_ONLY_PASSWORD = "read-only-password";

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
	private UserAccountRepository userAccountRepository;

	@Autowired
	private UserRoleAssignmentRepository roleAssignmentRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private Clock clock;

	@Test
	void importsPemCertificateAndExposesListAndDetailApis() throws Exception {
		BootstrapAdminResponse bootstrap = TestBootstrap.createAdmin(restTemplate, baseUrl());

		CertificateImportResponse imported = importCertificate(
			bootstrap.tenantId(),
			CertificatePemFixtures.INVENTORY_CERTIFICATE,
			CertificatePemFixtures.ROOT_CERTIFICATE,
			"platform-team",
			false,
			Set.of("Prod", "Web"));

		assertThat(imported.imported()).isTrue();
		assertThat(imported.status()).isEqualTo(CertificateStatus.ACTIVE);
		assertThat(certificateRepository.count()).isEqualTo(1);
		assertThat(versionRepository.count()).isEqualTo(1);

		ResponseEntity<String> listResponse = TestBootstrap.adminClient(restTemplate)
			.getForEntity(url("/api/v1/certificates?tenantId=" + bootstrap.tenantId()), String.class);

		assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		JsonNode list = objectMapper.readTree(listResponse.getBody());
		assertThat(list.path("totalElements").asInt()).isEqualTo(1);
		JsonNode summary = list.path("content").get(0);
		assertThat(summary.path("owner").asText()).isEqualTo("platform-team");
		assertThat(summary.path("subject").asText()).contains("CN=inventory.example.test");
		assertThat(summary.path("subjectAlternativeNames").toString()).contains("DNS:inventory.example.test");
		assertThat(summary.path("tags").toString()).contains("prod", "web");

		ResponseEntity<String> detailResponse = TestBootstrap.adminClient(restTemplate)
			.getForEntity(url("/api/v1/certificates/" + imported.certificateId()), String.class);

		assertThat(detailResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		JsonNode detail = objectMapper.readTree(detailResponse.getBody());
		assertThat(detail.path("currentVersion").path("publicKeyAlgorithm").asText()).isEqualTo("RSA");
		assertThat(detail.path("currentVersion").path("chainLength").asInt()).isEqualTo(2);
		assertThat(detail.path("chain")).hasSize(1);
		assertThat(detail.path("auditTimeline")).isNotEmpty();
	}

	@Test
	void duplicateImportReusesExistingVersionAndExpiredImportIsMarkedExpired() {
		BootstrapAdminResponse bootstrap = TestBootstrap.createAdmin(restTemplate, baseUrl());

		CertificateImportResponse first = importCertificate(
			bootstrap.tenantId(),
			CertificatePemFixtures.INVENTORY_CERTIFICATE,
			null,
			"platform-team",
			false,
			Set.of());
		CertificateImportResponse duplicate = importCertificate(
			bootstrap.tenantId(),
			CertificatePemFixtures.INVENTORY_CERTIFICATE,
			null,
			"platform-team",
			false,
			Set.of());
		CertificateImportResponse expired = importCertificate(
			bootstrap.tenantId(),
			CertificatePemFixtures.EXPIRED_CERTIFICATE,
			null,
			null,
			true,
			Set.of("legacy"));

		assertThat(duplicate.imported()).isFalse();
		assertThat(duplicate.certificateId()).isEqualTo(first.certificateId());
		assertThat(duplicate.versionId()).isEqualTo(first.versionId());
		assertThat(expired.status()).isEqualTo(CertificateStatus.EXPIRED);
		assertThat(certificateRepository.count()).isEqualTo(2);
		assertThat(versionRepository.count()).isEqualTo(2);
	}

	@Test
	void listSupportsSearchFiltersAndSorting() throws Exception {
		BootstrapAdminResponse bootstrap = TestBootstrap.createAdmin(restTemplate, baseUrl());
		importCertificate(bootstrap.tenantId(), CertificatePemFixtures.INVENTORY_CERTIFICATE, null, "platform-team", false, Set.of("prod"));
		importCertificate(bootstrap.tenantId(), CertificatePemFixtures.EXPIRED_CERTIFICATE, null, null, true, Set.of("legacy"));

		ResponseEntity<String> ownerResponse = TestBootstrap.adminClient(restTemplate)
			.getForEntity(url("/api/v1/certificates?tenantId=" + bootstrap.tenantId() + "&filter=owner:platform&filter=tag:prod"), String.class);
		assertThat(ownerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(objectMapper.readTree(ownerResponse.getBody()).path("totalElements").asInt()).isEqualTo(1);

		ResponseEntity<String> expiredResponse = TestBootstrap.adminClient(restTemplate)
			.getForEntity(url("/api/v1/certificates?tenantId=" + bootstrap.tenantId()
				+ "&filter=status:EXPIRED&filter=expiresBefore:2021-01-01T00:00:00Z&sort=expiresAt,desc"), String.class);
		assertThat(expiredResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		JsonNode expiredList = objectMapper.readTree(expiredResponse.getBody());
		assertThat(expiredList.path("totalElements").asInt()).isEqualTo(1);
		assertThat(expiredList.path("content").get(0).path("status").asText()).isEqualTo("EXPIRED");

		ResponseEntity<String> sanResponse = TestBootstrap.adminClient(restTemplate)
			.getForEntity(url("/api/v1/certificates?tenantId=" + bootstrap.tenantId() + "&filter=san:www.inventory"), String.class);
		assertThat(sanResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(objectMapper.readTree(sanResponse.getBody()).path("totalElements").asInt()).isEqualTo(1);
	}

	@Test
	void inventoryApisEnforceRbacAndTenantIsolation() throws Exception {
		BootstrapAdminResponse bootstrap = TestBootstrap.createAdmin(restTemplate, baseUrl());
		CertificateImportResponse defaultTenantCertificate = importCertificate(
			bootstrap.tenantId(),
			CertificatePemFixtures.INVENTORY_CERTIFICATE,
			null,
			"platform-team",
			false,
			Set.of("prod"));
		UUID secondTenantId = createTenant("second", "Second Tenant");
		CertificateImportResponse otherTenantCertificate = importCertificate(
			secondTenantId,
			CertificatePemFixtures.EXPIRED_CERTIFICATE,
			null,
			null,
			true,
			Set.of("legacy"));
		createReadOnlyUser(bootstrap.tenantId());

		TestRestTemplate readOnlyClient = restTemplate.withBasicAuth(READ_ONLY_EMAIL, READ_ONLY_PASSWORD);
		ResponseEntity<String> scopedList = readOnlyClient.getForEntity(url("/api/v1/certificates"), String.class);
		assertThat(scopedList.getStatusCode()).isEqualTo(HttpStatus.OK);
		JsonNode list = objectMapper.readTree(scopedList.getBody());
		assertThat(list.path("totalElements").asInt()).isEqualTo(1);
		assertThat(list.path("content").get(0).path("id").asText()).isEqualTo(defaultTenantCertificate.certificateId().toString());

		ResponseEntity<String> hiddenDetail = readOnlyClient.getForEntity(
			url("/api/v1/certificates/" + otherTenantCertificate.certificateId()),
			String.class);
		assertThat(hiddenDetail.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

		ResponseEntity<String> forbiddenImport = readOnlyClient.postForEntity(
			url("/api/v1/certificates/import"),
			new CertificateImportRequest(bootstrap.tenantId(), CertificatePemFixtures.EXPIRED_CERTIFICATE, null, null, true, Set.of()),
			String.class);
		assertThat(forbiddenImport.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	void importRejectsMalformedPemAndPrivateKeys() throws Exception {
		BootstrapAdminResponse bootstrap = TestBootstrap.createAdmin(restTemplate, baseUrl());

		ResponseEntity<String> malformed = TestBootstrap.adminClient(restTemplate)
			.postForEntity(
				url("/api/v1/certificates/import"),
				new CertificateImportRequest(bootstrap.tenantId(), "not a certificate", null, null, true, Set.of()),
				String.class);
		assertThat(malformed.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(objectMapper.readTree(malformed.getBody()).path("code").asText()).isEqualTo("VALIDATION_FAILED");

		ResponseEntity<String> privateKey = TestBootstrap.adminClient(restTemplate)
			.postForEntity(
				url("/api/v1/certificates/import"),
				new CertificateImportRequest(
					bootstrap.tenantId(),
					CertificatePemFixtures.INVENTORY_CERTIFICATE + "\n-----BEGIN PRIVATE KEY-----\nabc\n-----END PRIVATE KEY-----",
					null,
					null,
					true,
					Set.of()),
				String.class);
		assertThat(privateKey.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(objectMapper.readTree(privateKey.getBody()).path("message").asText()).doesNotContain("abc");
	}

	private CertificateImportResponse importCertificate(
			UUID tenantId,
			String certificatePem,
			String chainPem,
			String owner,
			boolean orphaned,
			Set<String> tags) {
		ResponseEntity<CertificateImportResponse> response = TestBootstrap.adminClient(restTemplate)
			.postForEntity(
				url("/api/v1/certificates/import"),
				new CertificateImportRequest(tenantId, certificatePem, chainPem, owner, orphaned, tags),
				CertificateImportResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		return response.getBody();
	}

	private UUID createTenant(String slug, String name) {
		ResponseEntity<TenantResponse> response = TestBootstrap.adminClient(restTemplate)
			.postForEntity(url("/api/v1/tenants"), new TenantCreateRequest(slug, name), TenantResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		return response.getBody().id();
	}

	private void createReadOnlyUser(UUID tenantId) {
		UserAccount readOnlyUser = userAccountRepository.save(new UserAccount(
			UUID.randomUUID(),
			READ_ONLY_EMAIL,
			"Certificate Reader",
			passwordEncoder.encode(READ_ONLY_PASSWORD),
			clock.instant()));
		roleAssignmentRepository.save(new UserRoleAssignment(
			UUID.randomUUID(),
			readOnlyUser.id(),
			tenantId,
			null,
			BuiltInRole.READ_ONLY,
			clock.instant()));
	}

	private String baseUrl() {
		return "http://localhost:" + port;
	}

	private String url(String path) {
		return baseUrl() + path;
	}
}
