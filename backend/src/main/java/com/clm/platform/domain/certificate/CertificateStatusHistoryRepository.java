package com.clm.platform.domain.certificate;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateStatusHistoryRepository extends JpaRepository<CertificateStatusHistory, UUID> {

	List<CertificateStatusHistory> findByCertificateIdOrderByChangedAtDesc(UUID certificateId);
}
