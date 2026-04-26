package com.clm.platform.domain.certificate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateVersionRepository extends JpaRepository<CertificateVersion, UUID> {

	Optional<CertificateVersion> findByTenantIdAndSha256Fingerprint(UUID tenantId, String sha256Fingerprint);

	List<CertificateVersion> findByCertificateIdOrderByVersionNumberDesc(UUID certificateId);
}
