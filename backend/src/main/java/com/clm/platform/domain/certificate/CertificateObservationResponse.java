package com.clm.platform.domain.certificate;

import java.time.Instant;
import java.util.UUID;

public record CertificateObservationResponse(
	UUID certificateId,
	UUID versionId,
	UUID observationId,
	boolean importedCertificate,
	boolean createdObservation,
	int observationCount,
	Instant lastSeenAt) {

	static CertificateObservationResponse from(
			ManagedCertificate certificate,
			CertificateVersion version,
			CertificateSourceObservation observation,
			boolean importedCertificate,
			boolean createdObservation) {
		return new CertificateObservationResponse(
			certificate.id(),
			version.id(),
			observation.id(),
			importedCertificate,
			createdObservation,
			observation.observationCount(),
			observation.lastSeenAt());
	}
}
