package com.clm.platform.domain.certificate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.clm.platform.api.error.ApiException;

class CertificatePrivateKeyValidatorTests {

	private final CertificatePemParser parser = newParser();

	private final CertificatePrivateKeyValidator validator = new CertificatePrivateKeyValidator();

	@Test
	void validatesMatchingRsaPrivateKey() {
		ParsedCertificate certificate = parser.parse(CertificatePemFixtures.MATCHING_RSA_CERTIFICATE, null);

		PrivateKeyMatchResult result = validator.validateMatches(
			certificate.publicKey(),
			CertificatePemFixtures.MATCHING_RSA_PRIVATE_KEY);

		assertThat(result.keyAlgorithm()).isEqualTo("RSA");
		assertThat(result.keyBlockLabel()).isEqualTo("PRIVATE KEY");
	}

	@Test
	void validatesMatchingEcPrivateKey() {
		ParsedCertificate certificate = parser.parse(CertificatePemFixtures.MATCHING_EC_CERTIFICATE, null);

		PrivateKeyMatchResult result = validator.validateMatches(
			certificate.publicKey(),
			CertificatePemFixtures.MATCHING_EC_PRIVATE_KEY);

		assertThat(result.keyAlgorithm()).isEqualTo("EC");
		assertThat(result.keyBlockLabel()).isEqualTo("PRIVATE KEY");
	}

	@Test
	void rejectsMismatchedAlgorithmsAndKeysWithoutEchoingMaterial() {
		ParsedCertificate certificate = parser.parse(CertificatePemFixtures.MATCHING_RSA_CERTIFICATE, null);

		assertThatThrownBy(() -> validator.validateMatches(certificate.publicKey(), CertificatePemFixtures.MATCHING_EC_PRIVATE_KEY))
			.isInstanceOf(ApiException.class)
			.hasMessage("Private key algorithm does not match certificate public key algorithm.")
			.hasMessageNotContaining("MEECAQ");
	}

	@Test
	void rejectsMalformedEncryptedAndLegacyPrivateKeyPemSafely() {
		ParsedCertificate certificate = parser.parse(CertificatePemFixtures.MATCHING_RSA_CERTIFICATE, null);

		assertThatThrownBy(() -> validator.validateMatches(certificate.publicKey(), CertificatePemFixtures.PRIVATE_KEY))
			.isInstanceOf(ApiException.class)
			.hasMessage("Private key PEM could not be parsed as unencrypted PKCS#8 RSA or EC key material.")
			.hasMessageNotContaining("abc-secret-key-body");

		assertThatThrownBy(() -> validator.validateMatches(certificate.publicKey(), """
			-----BEGIN ENCRYPTED PRIVATE KEY-----
			abc-secret-key-body
			-----END ENCRYPTED PRIVATE KEY-----
			"""))
			.isInstanceOf(ApiException.class)
			.hasMessage("Encrypted private keys are not supported by this API path.")
			.hasMessageNotContaining("abc-secret-key-body");

		assertThatThrownBy(() -> validator.validateMatches(certificate.publicKey(), """
			-----BEGIN RSA PRIVATE KEY-----
			abc-secret-key-body
			-----END RSA PRIVATE KEY-----
			"""))
			.isInstanceOf(ApiException.class)
			.hasMessage("Private key PEM must use unencrypted PKCS#8 PRIVATE KEY format.")
			.hasMessageNotContaining("abc-secret-key-body");
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
