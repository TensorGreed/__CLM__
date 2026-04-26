package com.clm.platform.domain.certificate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateSourceObservationRepository extends JpaRepository<CertificateSourceObservation, UUID> {

	Optional<CertificateSourceObservation> findByTenantIdAndSourceTypeAndSourceKeyAndObservedResourceKeyAndCertificateVersionId(
			UUID tenantId,
			String sourceType,
			String sourceKey,
			String observedResourceKey,
			UUID certificateVersionId);

	List<CertificateSourceObservation> findByCertificateIdOrderByLastSeenAtDesc(UUID certificateId);
}
