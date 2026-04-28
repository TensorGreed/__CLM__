package com.clm.platform.domain.certificate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.clm.platform.api.error.ApiException;

class PemSafetyInspectorTests {

	@Test
	void summarizesPemLabelsWithoutReturningBodies() {
		var summaries = PemSafetyInspector.summarize("""
			-----BEGIN CERTIFICATE-----
			public-body
			-----END CERTIFICATE-----
			-----BEGIN EC PRIVATE KEY-----
			secret-body
			-----END EC PRIVATE KEY-----
			""");

		assertThat(summaries).hasSize(2);
		assertThat(summaries.get(0).label()).isEqualTo("CERTIFICATE");
		assertThat(summaries.get(0).privateKey()).isFalse();
		assertThat(summaries.get(1).label()).isEqualTo("EC PRIVATE KEY");
		assertThat(summaries.get(1).privateKey()).isTrue();
		assertThat(summaries.toString()).doesNotContain("secret-body");
	}

	@Test
	void rejectsPrivateKeyMaterialWithFieldScopedMessage() {
		assertThatThrownBy(() -> PemSafetyInspector.rejectPrivateKeyMaterial(
			CertificatePemFixtures.PRIVATE_KEY,
			"Certificate PEM"))
			.isInstanceOf(ApiException.class)
			.hasMessage("Certificate PEM must not contain private key material.")
			.hasMessageNotContaining("abc-secret-key-body");
	}

	@Test
	void requiresPrivateKeyPemBlockForFutureKeyImportPath() {
		assertThat(PemSafetyInspector.requirePrivateKeyMaterial(
			CertificatePemFixtures.PRIVATE_KEY,
			"Private key PEM").label()).isEqualTo("PRIVATE KEY");

		assertThatThrownBy(() -> PemSafetyInspector.requirePrivateKeyMaterial("not a key", "Private key PEM"))
			.isInstanceOf(ApiException.class)
			.hasMessage("Private key PEM must contain a private key PEM block.");
	}
}
