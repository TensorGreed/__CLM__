package com.clm.platform.domain.certificate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.clm.platform.api.error.ApiException;

class CertificatePemParserTests {

	private final CertificatePemParser parser = newParser();

	@Test
	void parsesLeafMetadataAndChainMetadata() {
		ParsedCertificate parsed = parser.parse(
			CertificatePemFixtures.INVENTORY_CERTIFICATE,
			CertificatePemFixtures.ROOT_CERTIFICATE);

		assertThat(parsed.subjectDn()).contains("CN=inventory.example.test");
		assertThat(parsed.commonName()).isEqualTo("inventory.example.test");
		assertThat(parsed.issuerDn()).contains("CN=inventory.example.test");
		assertThat(parsed.publicKeyAlgorithm()).isEqualTo("RSA");
		assertThat(parsed.signatureAlgorithm()).contains("RSA");
		assertThat(parsed.sha256Fingerprint()).hasSize(95);
		assertThat(parsed.subjectAlternativeNames())
			.contains("DNS:inventory.example.test", "DNS:www.inventory.example.test", "IP:127.0.0.1");
		assertThat(parsed.chain()).hasSize(1);
		assertThat(parsed.chain().getFirst().subjectDn()).contains("CN=CLM Test Root");
		assertThat(parsed.chainLength()).isEqualTo(2);
	}

	@Test
	void rejectsMalformedPem() {
		assertThatThrownBy(() -> parser.parse("not a certificate", null))
			.isInstanceOf(ApiException.class)
			.hasMessageContaining("could not be parsed");
	}

	@Test
	void rejectsPrivateKeyMaterial() {
		String privateKey = """
			-----BEGIN PRIVATE KEY-----
			abc
			-----END PRIVATE KEY-----
			""";

		assertThatThrownBy(() -> parser.parse(CertificatePemFixtures.INVENTORY_CERTIFICATE + privateKey, null))
			.isInstanceOf(ApiException.class)
			.hasMessageContaining("Private key material");
	}

	private static CertificatePemParser newParser() {
		try {
			return new CertificatePemParser();
		}
		catch (java.security.cert.CertificateException exception) {
			throw new IllegalStateException(exception);
		}
	}
}
