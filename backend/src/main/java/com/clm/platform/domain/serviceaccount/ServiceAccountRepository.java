package com.clm.platform.domain.serviceaccount;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceAccountRepository extends JpaRepository<ServiceAccount, UUID> {

	List<ServiceAccount> findByTenantId(UUID tenantId);

	List<ServiceAccount> findByTenantIdOrderByNameAsc(UUID tenantId);

	List<ServiceAccount> findByTenantIdInOrderByNameAsc(List<UUID> tenantIds);

	List<ServiceAccount> findAllByOrderByNameAsc();
}
