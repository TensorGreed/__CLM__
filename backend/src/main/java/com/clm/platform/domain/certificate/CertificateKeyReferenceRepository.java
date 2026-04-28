package com.clm.platform.domain.certificate;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateKeyReferenceRepository extends JpaRepository<CertificateKeyReference, UUID> {

	Optional<CertificateKeyReference> findByCertificateVersionId(UUID certificateVersionId);
}
