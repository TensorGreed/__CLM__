package com.clm.platform.domain.serviceaccount;

import java.util.UUID;

public record ServiceAccountResponse(UUID id, UUID tenantId, String name, ServiceAccountStatus status) {

	static ServiceAccountResponse from(ServiceAccount serviceAccount) {
		return new ServiceAccountResponse(
			serviceAccount.id(),
			serviceAccount.tenantId(),
			serviceAccount.name(),
			serviceAccount.status());
	}
}
