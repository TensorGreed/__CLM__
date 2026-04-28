package com.clm.platform.domain.certificate;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.clm.platform.api.error.ApiErrorCode;
import com.clm.platform.api.error.ApiException;

@Service
public class CertificatePrivateKeyValidator {

	private static final Pattern PRIVATE_KEY_BLOCK = Pattern.compile(
		"-----BEGIN\\s+([A-Z0-9][A-Z0-9 ._-]*?)\\s*-----\\s*([A-Za-z0-9+/=\\s]+?)\\s*-----END\\s+\\1\\s*-----",
		Pattern.CASE_INSENSITIVE);

	private static final byte[] VALIDATION_CHALLENGE =
		"clm-private-key-validation-v1".getBytes(StandardCharsets.UTF_8);

	public PrivateKeyMatchResult validateMatches(PublicKey certificatePublicKey, String privateKeyPem) {
		if (certificatePublicKey == null) {
			throw validation("Certificate public key is required for private key match validation.");
		}

		String certificateAlgorithm = normalizedAlgorithm(certificatePublicKey.getAlgorithm());
		if (!isSupportedAlgorithm(certificateAlgorithm)) {
			throw validation("Certificate public key algorithm is not supported for private key match validation.");
		}

		ParsedPrivateKey privateKey = parsePrivateKey(privateKeyPem);
		if (!certificateAlgorithm.equals(privateKey.algorithm())) {
			throw validation("Private key algorithm does not match certificate public key algorithm.");
		}
		if (!signatureMatches(certificateAlgorithm, certificatePublicKey, privateKey.privateKey())) {
			throw validation("Private key does not match certificate public key.");
		}
		return new PrivateKeyMatchResult(privateKey.algorithm(), privateKey.label());
	}

	private static ParsedPrivateKey parsePrivateKey(String privateKeyPem) {
		PemBlockSummary summary = PemSafetyInspector.requirePrivateKeyMaterial(privateKeyPem, "Private key PEM");
		if (summary.label().contains("ENCRYPTED")) {
			throw validation("Encrypted private keys are not supported by this API path.");
		}
		if (!"PRIVATE KEY".equals(summary.label())) {
			throw validation("Private key PEM must use unencrypted PKCS#8 PRIVATE KEY format.");
		}

		Matcher matcher = PRIVATE_KEY_BLOCK.matcher(privateKeyPem);
		if (!matcher.find()) {
			throw validation("Private key PEM could not be parsed as unencrypted PKCS#8 RSA or EC key material.");
		}
		byte[] der = decodeKeyBody(matcher.group(2));
		return parsePkcs8(der, summary.label());
	}

	private static byte[] decodeKeyBody(String body) {
		try {
			return Base64.getMimeDecoder().decode(body);
		}
		catch (IllegalArgumentException exception) {
			throw validation("Private key PEM could not be parsed as unencrypted PKCS#8 RSA or EC key material.");
		}
	}

	private static ParsedPrivateKey parsePkcs8(byte[] der, String label) {
		for (String algorithm : new String[] { "RSA", "EC" }) {
			try {
				PrivateKey privateKey = KeyFactory.getInstance(algorithm).generatePrivate(new PKCS8EncodedKeySpec(der));
				return new ParsedPrivateKey(algorithm, label, privateKey);
			}
			catch (GeneralSecurityException exception) {
				// Try the next approved algorithm before returning a safe validation error.
			}
		}
		throw validation("Private key PEM could not be parsed as unencrypted PKCS#8 RSA or EC key material.");
	}

	private static boolean signatureMatches(String algorithm, PublicKey publicKey, PrivateKey privateKey) {
		try {
			Signature signer = Signature.getInstance(signatureAlgorithm(algorithm));
			signer.initSign(privateKey);
			signer.update(VALIDATION_CHALLENGE);
			byte[] signature = signer.sign();

			Signature verifier = Signature.getInstance(signatureAlgorithm(algorithm));
			verifier.initVerify(publicKey);
			verifier.update(VALIDATION_CHALLENGE);
			return verifier.verify(signature);
		}
		catch (GeneralSecurityException exception) {
			throw validation("Private key could not be validated against certificate public key.");
		}
	}

	private static String signatureAlgorithm(String algorithm) {
		return switch (algorithm) {
			case "RSA" -> "SHA256withRSA";
			case "EC" -> "SHA256withECDSA";
			default -> throw validation("Certificate public key algorithm is not supported for private key match validation.");
		};
	}

	private static String normalizedAlgorithm(String algorithm) {
		String normalized = algorithm == null ? "" : algorithm.trim().toUpperCase(Locale.ROOT);
		return "ECDSA".equals(normalized) ? "EC" : normalized;
	}

	private static boolean isSupportedAlgorithm(String algorithm) {
		return "RSA".equals(algorithm) || "EC".equals(algorithm);
	}

	private static ApiException validation(String message) {
		return new ApiException(ApiErrorCode.VALIDATION_FAILED, message);
	}

	private record ParsedPrivateKey(String algorithm, String label, PrivateKey privateKey) {
	}
}
