package com.clm.platform.domain.certificate;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface CertificateMetadataEntryRepository extends JpaRepository<CertificateMetadataEntry, UUID> {

	List<CertificateMetadataEntry> findByCertificateIdOrderByKeyAsc(UUID certificateId);

	@Modifying
	void deleteByCertificateId(UUID certificateId);
}
