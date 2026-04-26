package com.clm.platform.domain.certificate;

import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Pattern;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clm.platform.api.error.ApiErrorCode;
import com.clm.platform.api.error.ApiException;
import com.clm.platform.api.error.ResourceNotFoundException;
import com.clm.platform.api.query.SortDirection;
import com.clm.platform.api.query.SortSpec;
import com.clm.platform.domain.audit.AuditDecision;
import com.clm.platform.domain.audit.AuditEventCommand;
import com.clm.platform.domain.audit.AuditEventRepository;
import com.clm.platform.domain.audit.AuditEventService;
import com.clm.platform.domain.audit.AuditStatus;
import com.clm.platform.domain.tenancy.TenancyService;
import com.clm.platform.security.CurrentActor;

@Service
public class CertificateInventoryService {

	private static final Pattern TAG_PATTERN = Pattern.compile("[a-z0-9][a-z0-9._:-]{0,63}");

	private final ManagedCertificateRepository certificateRepository;

	private final CertificateVersionRepository versionRepository;

	private final CertificateChainEntryRepository chainEntryRepository;

	private final CertificatePemParser certificatePemParser;

	private final TenancyService tenancyService;

	private final AuditEventService auditEventService;

	private final AuditEventRepository auditEventRepository;

	private final Clock clock;

	public CertificateInventoryService(
			ManagedCertificateRepository certificateRepository,
			CertificateVersionRepository versionRepository,
			CertificateChainEntryRepository chainEntryRepository,
			CertificatePemParser certificatePemParser,
			TenancyService tenancyService,
			AuditEventService auditEventService,
			AuditEventRepository auditEventRepository,
			Clock clock) {
		this.certificateRepository = certificateRepository;
		this.versionRepository = versionRepository;
		this.chainEntryRepository = chainEntryRepository;
		this.certificatePemParser = certificatePemParser;
		this.tenancyService = tenancyService;
		this.auditEventService = auditEventService;
		this.auditEventRepository = auditEventRepository;
		this.clock = clock;
	}

	@Transactional
	public CertificateImportResponse importCertificate(CertificateImportRequest request) {
		tenancyService.getTenant(request.tenantId());
		ParsedCertificate parsedCertificate = certificatePemParser.parse(request.certificatePem(), request.chainPem());

		return versionRepository.findByTenantIdAndSha256Fingerprint(request.tenantId(), parsedCertificate.sha256Fingerprint())
			.map(existingVersion -> duplicateImportResponse(request.tenantId(), existingVersion))
			.orElseGet(() -> createImportedCertificate(request, parsedCertificate));
	}

	@Transactional(readOnly = true)
	public Page<CertificateSummaryResponse> list(CertificateSearchRequest request) {
		PageRequest pageRequest = PageRequest.of(
			request.page().page(),
			request.page().size(),
			toSort(request.sorts()));

		return certificateRepository.findAll(specification(request), pageRequest)
			.map(CertificateSummaryResponse::from);
	}

	@Transactional(readOnly = true)
	public CertificateDetailResponse get(UUID certificateId) {
		ManagedCertificate certificate = certificateRepository.findById(certificateId)
			.filter(this::canAccess)
			.orElseThrow(() -> new ResourceNotFoundException("Certificate", certificateId));
		CertificateVersion currentVersion = versionRepository.findById(certificate.currentVersionId())
			.orElseThrow(() -> new ResourceNotFoundException("CertificateVersion", certificate.currentVersionId()));

		return CertificateDetailResponse.from(
			certificate,
			currentVersion,
			versionRepository.findByCertificateIdOrderByVersionNumberDesc(certificate.id()),
			chainEntryRepository.findByCertificateVersionIdOrderByPositionAsc(currentVersion.id()),
			auditEventRepository.findTop25ByResourceTypeAndResourceIdOrderByOccurredAtDesc(
				"certificate",
				certificate.id().toString()));
	}

	private CertificateImportResponse duplicateImportResponse(UUID tenantId, CertificateVersion existingVersion) {
		ManagedCertificate certificate = certificateRepository.findById(existingVersion.certificateId())
			.filter(this::canAccess)
			.orElseThrow(() -> new ResourceNotFoundException("Certificate", existingVersion.certificateId()));
		appendAuditEvent(
			tenantId,
			"certificate.import_duplicate",
			certificate.id(),
			"Duplicate certificate import reused existing version.",
			"{\"sha256Fingerprint\":\"" + existingVersion.sha256Fingerprint() + "\"}");
		return CertificateImportResponse.from(certificate, existingVersion, false);
	}

	private CertificateImportResponse createImportedCertificate(CertificateImportRequest request, ParsedCertificate parsedCertificate) {
		Instant now = clock.instant();
		String owner = normalizedOwner(request.owner());
		Set<String> tags = normalizeTags(request.tags());
		CertificateStatus status = parsedCertificate.notAfter().isBefore(now) ? CertificateStatus.EXPIRED : CertificateStatus.ACTIVE;
		ManagedCertificate certificate = certificateRepository.save(ManagedCertificate.create(
			request.tenantId(),
			owner,
			owner == null,
			status,
			parsedCertificate,
			tags,
			now));
		CertificateVersion version = versionRepository.save(CertificateVersion.importVersion(
			certificate.id(),
			request.tenantId(),
			parsedCertificate,
			now));
		for (int index = 0; index < parsedCertificate.chain().size(); index++) {
			chainEntryRepository.save(CertificateChainEntry.from(version.id(), index + 1, parsedCertificate.chain().get(index)));
		}
		certificate.assignCurrentVersion(version.id(), now);

		appendAuditEvent(
			request.tenantId(),
			"certificate.imported",
			certificate.id(),
			"Certificate imported.",
			"{\"sha256Fingerprint\":\"" + version.sha256Fingerprint() + "\",\"status\":\"" + status.name() + "\"}");
		return CertificateImportResponse.from(certificate, version, true);
	}

	private Specification<ManagedCertificate> specification(CertificateSearchRequest request) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (request.tenantId() != null) {
				tenancyService.getTenant(request.tenantId());
				predicates.add(criteriaBuilder.equal(root.get("tenantId"), request.tenantId()));
			}
			else if (!CurrentActor.hasGlobalAccess()) {
				Set<UUID> tenantIds = CurrentActor.tenantIds();
				if (tenantIds.isEmpty()) {
					return criteriaBuilder.disjunction();
				}
				predicates.add(root.get("tenantId").in(tenantIds));
			}

			for (var filter : request.filters()) {
				String field = filter.field().toLowerCase(Locale.ROOT);
				String value = filter.value().trim();
				switch (field) {
					case "owner" -> predicates.add(containsIgnoreCase(root.get("owner"), value, criteriaBuilder));
					case "status" -> predicates.add(criteriaBuilder.equal(root.get("status"), parseStatus(value)));
					case "issuer" -> predicates.add(containsIgnoreCase(root.get("issuerDn"), value, criteriaBuilder));
					case "subject" -> predicates.add(containsIgnoreCase(root.get("subjectDn"), value, criteriaBuilder));
					case "san" -> predicates.add(containsIgnoreCase(root.get("sans"), value, criteriaBuilder));
					case "expiresbefore" -> predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("notAfter"), parseInstant(value)));
					case "expiresafter" -> predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("notAfter"), parseInstant(value)));
					case "tag" -> {
						if (query != null) {
							query.distinct(true);
						}
						predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.joinSet("tags")), normalizeTag(value)));
					}
					default -> throw new ApiException(ApiErrorCode.VALIDATION_FAILED, "Unsupported certificate filter field: " + filter.field());
				}
			}
			return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
		};
	}

	private static Predicate containsIgnoreCase(Expression<String> expression, String value, jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder) {
		return criteriaBuilder.like(
			criteriaBuilder.lower(expression),
			"%" + value.toLowerCase(Locale.ROOT) + "%");
	}

	private static Sort toSort(List<SortSpec> sorts) {
		if (sorts.isEmpty()) {
			return Sort.by(Sort.Direction.ASC, "notAfter");
		}
		return Sort.by(sorts.stream().map(sort -> new Sort.Order(direction(sort.direction()), sortProperty(sort.field()))).toList());
	}

	private static Sort.Direction direction(SortDirection direction) {
		return direction == SortDirection.DESC ? Sort.Direction.DESC : Sort.Direction.ASC;
	}

	private static String sortProperty(String field) {
		return switch (field.toLowerCase(Locale.ROOT)) {
			case "createdat" -> "createdAt";
			case "updatedat" -> "updatedAt";
			case "expiresat", "validto" -> "notAfter";
			case "subject" -> "subjectDn";
			case "issuer" -> "issuerDn";
			case "owner" -> "owner";
			case "status" -> "status";
			default -> throw new ApiException(ApiErrorCode.VALIDATION_FAILED, "Unsupported certificate sort field: " + field);
		};
	}

	private boolean canAccess(ManagedCertificate certificate) {
		return CurrentActor.hasGlobalAccess() || CurrentActor.tenantIds().contains(certificate.tenantId());
	}

	private static CertificateStatus parseStatus(String value) {
		try {
			return CertificateStatus.valueOf(value.toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException exception) {
			throw new ApiException(ApiErrorCode.VALIDATION_FAILED, "Unsupported certificate status filter value.");
		}
	}

	private static Instant parseInstant(String value) {
		try {
			return Instant.parse(value);
		}
		catch (DateTimeParseException exception) {
			throw new ApiException(ApiErrorCode.VALIDATION_FAILED, "Expiry filters must use ISO-8601 instants.");
		}
	}

	private static String normalizedOwner(String owner) {
		if (owner == null || owner.isBlank()) {
			return null;
		}
		return owner.trim();
	}

	private static Set<String> normalizeTags(Set<String> tags) {
		if (tags == null || tags.isEmpty()) {
			return Set.of();
		}
		Set<String> normalized = new TreeSet<>();
		for (String tag : tags) {
			normalized.add(normalizeTag(tag));
		}
		return normalized;
	}

	private static String normalizeTag(String tag) {
		String normalized = tag == null ? "" : tag.trim().toLowerCase(Locale.ROOT);
		if (!TAG_PATTERN.matcher(normalized).matches()) {
			throw new ApiException(
				ApiErrorCode.VALIDATION_FAILED,
				"Tags must be 1-64 lowercase letters, numbers, dots, underscores, colons, or dashes.");
		}
		return normalized;
	}

	private void appendAuditEvent(UUID tenantId, String action, UUID certificateId, String reason, String metadata) {
		auditEventService.append(new AuditEventCommand(
			CurrentActor.actorType(),
			CurrentActor.actorId(),
			tenantId.toString(),
			action,
			"certificate",
			certificateId.toString(),
			AuditDecision.ALLOW,
			AuditStatus.SUCCESS,
			reason,
			null,
			metadata));
	}
}
