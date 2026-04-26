package com.clm.platform.domain.identity;

import java.util.UUID;

public record BootstrapAdminResponse(UUID userId, UUID tenantId, UUID organizationId, String email) {
}
