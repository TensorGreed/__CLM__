package com.clm.platform.domain.certificate;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record CertificateSourceObservationResponse(
	UUID id,
	UUID certificateVersionId,
	String sourceType,
	String sourceKey,
	String observedResourceKey,
	String sourceName,
	String sha256Fingerprint,
	Map<String, String> metadata,
	Instant firstSeenAt,
	Instant lastSeenAt,
	int observationCount) {

	static CertificateSourceObservationResponse from(CertificateSourceObservation observation) {
		return new CertificateSourceObservationResponse(
			observation.id(),
			observation.certificateVersionId(),
			observation.sourceType(),
			observation.sourceKey(),
			observation.observedResourceKey(),
			observation.sourceName(),
			observation.sha256Fingerprint(),
			CertificateTextValues.unpackMap(observation.metadata()),
			observation.firstSeenAt(),
			observation.lastSeenAt(),
			observation.observationCount());
	}
}
