package com.clm.platform.domain.certificate;

import java.util.List;
import java.util.UUID;

import com.clm.platform.api.query.FilterSpec;
import com.clm.platform.api.query.PageSpec;
import com.clm.platform.api.query.SortSpec;

public record CertificateSearchRequest(
	UUID tenantId,
	PageSpec page,
	List<FilterSpec> filters,
	List<SortSpec> sorts) {
}
