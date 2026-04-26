package com.clm.platform.domain.certificate;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

import org.springframework.stereotype.Service;

import com.clm.platform.api.error.ApiErrorCode;
import com.clm.platform.api.error.ApiException;

@Service
public class CertificatePemParser {

	private static final Pattern PRIVATE_KEY_PATTERN = Pattern.compile(
		"-----BEGIN [A-Z0-9 ]*PRIVATE KEY-----",
		Pattern.CASE_INSENSITIVE);

	private final CertificateFactory certificateFactory;

	public CertificatePemParser() throws CertificateException {
		this.certificateFactory = CertificateFactory.getInstance("X.509");
	}

	public ParsedCertificate parse(String certificatePem, String chainPem) {
		rejectPrivateKeyMaterial(certificatePem);
		rejectPrivateKeyMaterial(chainPem);

		List<X509Certificate> leafCertificates = readCertificates(certificatePem, "Certificate PEM");
		if (leafCertificates.size() != 1) {
			throw validation("Certificate PEM must contain exactly one leaf certificate.");
		}

		X509Certificate leaf = leafCertificates.getFirst();
		List<ParsedChainCertificate> chain = readOptionalChain(chainPem);
		return new ParsedCertificate(
			normalizePem(certificatePem),
			distinguishedName(leaf.getSubjectX500Principal()),
			commonName(leaf),
			distinguishedName(leaf.getIssuerX500Principal()),
			serialNumber(leaf.getSerialNumber()),
			leaf.getNotBefore().toInstant(),
			leaf.getNotAfter().toInstant(),
			fingerprint("SHA-256", leaf),
			fingerprint("SHA-1", leaf),
			leaf.getPublicKey().getAlgorithm(),
			leaf.getSigAlgName(),
			subjectAlternativeNames(leaf),
			selfSigned(leaf),
			chain);
	}

	private List<ParsedChainCertificate> readOptionalChain(String chainPem) {
		if (chainPem == null || chainPem.isBlank()) {
			return List.of();
		}
		List<X509Certificate> certificates = readCertificates(chainPem, "Chain PEM");
		List<ParsedChainCertificate> chain = new ArrayList<>();
		for (int index = 0; index < certificates.size(); index++) {
			X509Certificate certificate = certificates.get(index);
			chain.add(new ParsedChainCertificate(
				distinguishedName(certificate.getSubjectX500Principal()),
				distinguishedName(certificate.getIssuerX500Principal()),
				serialNumber(certificate.getSerialNumber()),
				certificate.getNotBefore().toInstant(),
				certificate.getNotAfter().toInstant(),
				fingerprint("SHA-256", certificate),
				fingerprint("SHA-1", certificate),
				selfSigned(certificate)));
		}
		return chain;
	}

	private List<X509Certificate> readCertificates(String pem, String fieldName) {
		if (pem == null || pem.isBlank()) {
			throw validation(fieldName + " cannot be blank.");
		}
		try {
			ByteArrayInputStream input = new ByteArrayInputStream(pem.getBytes(StandardCharsets.US_ASCII));
			Collection<? extends java.security.cert.Certificate> certificates = certificateFactory.generateCertificates(input);
			return certificates.stream()
				.map(X509Certificate.class::cast)
				.toList();
		}
		catch (CertificateException | IllegalArgumentException exception) {
			throw validation(fieldName + " could not be parsed as X.509 PEM.");
		}
	}

	private static void rejectPrivateKeyMaterial(String pem) {
		if (pem != null && PRIVATE_KEY_PATTERN.matcher(pem).find()) {
			throw validation("Private key material is not accepted by certificate inventory import.");
		}
	}

	private static String normalizePem(String pem) {
		return pem.trim() + "\n";
	}

	private static String distinguishedName(javax.security.auth.x500.X500Principal principal) {
		return principal.getName(javax.security.auth.x500.X500Principal.RFC2253);
	}

	private static String commonName(X509Certificate certificate) {
		try {
			LdapName name = new LdapName(distinguishedName(certificate.getSubjectX500Principal()));
			for (var rdn : name.getRdns()) {
				if ("CN".equalsIgnoreCase(rdn.getType())) {
					return String.valueOf(rdn.getValue());
				}
			}
		}
		catch (InvalidNameException exception) {
			return null;
		}
		return null;
	}

	private static String serialNumber(BigInteger serialNumber) {
		return serialNumber.toString(16).toUpperCase(Locale.ROOT);
	}

	private static String fingerprint(String algorithm, X509Certificate certificate) {
		try {
			byte[] digest = MessageDigest.getInstance(algorithm).digest(certificate.getEncoded());
			return HexFormat.ofDelimiter(":").withUpperCase().formatHex(digest);
		}
		catch (CertificateException | NoSuchAlgorithmException exception) {
			throw validation("Certificate fingerprint could not be calculated.");
		}
	}

	private static List<String> subjectAlternativeNames(X509Certificate certificate) {
		try {
			Collection<List<?>> names = certificate.getSubjectAlternativeNames();
			if (names == null || names.isEmpty()) {
				return List.of();
			}
			List<String> values = new ArrayList<>();
			for (List<?> name : names) {
				if (name.size() >= 2 && name.get(0) instanceof Integer type) {
					values.add(subjectAlternativeName(type, name.get(1)));
				}
			}
			return values.stream().sorted().toList();
		}
		catch (CertificateException exception) {
			throw validation("Certificate subject alternative names could not be parsed.");
		}
	}

	private static String subjectAlternativeName(int type, Object value) {
		String resolvedValue = String.valueOf(value);
		return switch (type) {
			case 1 -> "EMAIL:" + resolvedValue;
			case 2 -> "DNS:" + resolvedValue;
			case 6 -> "URI:" + resolvedValue;
			case 7 -> "IP:" + resolvedValue;
			default -> "TYPE_" + type + ":" + resolvedValue;
		};
	}

	private static boolean selfSigned(X509Certificate certificate) {
		if (!certificate.getSubjectX500Principal().equals(certificate.getIssuerX500Principal())) {
			return false;
		}
		try {
			certificate.verify(certificate.getPublicKey());
			return true;
		}
		catch (GeneralSecurityException exception) {
			return false;
		}
	}

	private static ApiException validation(String message) {
		return new ApiException(ApiErrorCode.VALIDATION_FAILED, message);
	}
}
