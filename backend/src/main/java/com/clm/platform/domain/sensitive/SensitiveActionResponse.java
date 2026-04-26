package com.clm.platform.domain.sensitive;

import java.util.UUID;

public record SensitiveActionResponse(UUID auditEventId, SensitiveActionType actionType, String resourceType, String resourceId) {
}
