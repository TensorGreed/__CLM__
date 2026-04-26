package com.clm.platform.domain.certificate;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.clm.platform.api.ApiPaths;
import com.clm.platform.api.dto.PageResponse;
import com.clm.platform.api.query.FilterSpec;
import com.clm.platform.api.query.PageSpec;
import com.clm.platform.api.query.SortSpec;

@RestController
public class CertificateController {

	private final CertificateInventoryService certificateInventoryService;

	public CertificateController(CertificateInventoryService certificateInventoryService) {
		this.certificateInventoryService = certificateInventoryService;
	}

	@PostMapping(ApiPaths.API_V1 + "/certificates/import")
	@PreAuthorize("hasAuthority('PERMISSION_CERTIFICATE_IMPORT')")
	CertificateImportResponse importCertificate(@Valid @RequestBody CertificateImportRequest request) {
		return certificateInventoryService.importCertificate(request);
	}

	@GetMapping(ApiPaths.API_V1 + "/certificates")
	@PreAuthorize("hasAuthority('PERMISSION_CERTIFICATE_READ')")
	PageResponse<CertificateSummaryResponse> listCertificates(
			@RequestParam(required = false) UUID tenantId,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size,
			@RequestParam(required = false) List<String> filter,
			@RequestParam(required = false) List<String> sort) {
		return PageResponse.from(certificateInventoryService.list(new CertificateSearchRequest(
			tenantId,
			PageSpec.from(page, size),
			FilterSpec.parse(filter),
			SortSpec.parse(sort))));
	}

	@GetMapping(ApiPaths.API_V1 + "/certificates/{certificateId}")
	@PreAuthorize("hasAuthority('PERMISSION_CERTIFICATE_READ')")
	CertificateDetailResponse getCertificate(@PathVariable UUID certificateId) {
		return certificateInventoryService.get(certificateId);
	}
}
