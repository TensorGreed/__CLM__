package com.clm.platform.domain.certificate;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateChainEntryRepository extends JpaRepository<CertificateChainEntry, UUID> {

	List<CertificateChainEntry> findByCertificateVersionIdOrderByPositionAsc(UUID certificateVersionId);
}
