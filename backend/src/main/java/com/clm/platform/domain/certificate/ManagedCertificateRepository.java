package com.clm.platform.domain.certificate;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ManagedCertificateRepository
		extends JpaRepository<ManagedCertificate, UUID>, JpaSpecificationExecutor<ManagedCertificate> {

	Optional<ManagedCertificate> findByTenantIdAndSha256Fingerprint(UUID tenantId, String sha256Fingerprint);
}
