package com.clm.platform.domain.certificate;

import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Pattern;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

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

	private static final Pattern METADATA_KEY_PATTERN = Pattern.compile("[a-z][a-z0-9._:-]{0,63}");

	private static final Pattern SOURCE_TYPE_PATTERN = Pattern.compile("[a-z0-9][a-z0-9._:-]{0,63}");

	private final ManagedCertificateRepository certificateRepository;

	private final CertificateVersionRepository versionRepository;

	private final CertificateChainEntryRepository chainEntryRepository;

	private final CertificateSourceObservationRepository sourceObservationRepository;

	private final CertificateMetadataEntryRepository metadataEntryRepository;

	private final CertificateStatusHistoryRepository statusHistoryRepository;

	private final CertificatePemParser certificatePemParser;

	private final TenancyService tenancyService;

	private final AuditEventService auditEventService;

	private final AuditEventRepository auditEventRepository;

	private final Clock clock;

	public CertificateInventoryService(
			ManagedCertificateRepository certificateRepository,
			CertificateVersionRepository versionRepository,
			CertificateChainEntryRepository chainEntryRepository,
			CertificateSourceObservationRepository sourceObservationRepository,
			CertificateMetadataEntryRepository metadataEntryRepository,
			CertificateStatusHistoryRepository statusHistoryRepository,
			CertificatePemParser certificatePemParser,
			TenancyService tenancyService,
			AuditEventService auditEventService,
			AuditEventRepository auditEventRepository,
			Clock clock) {
		this.certificateRepository = certificateRepository;
		this.versionRepository = versionRepository;
		this.chainEntryRepository = chainEntryRepository;
		this.sourceObservationRepository = sourceObservationRepository;
		this.metadataEntryRepository = metadataEntryRepository;
		this.statusHistoryRepository = statusHistoryRepository;
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

	@Transactional
	public CertificateObservationResponse observeCertificate(CertificateObservationRequest request) {
		tenancyService.getTenant(request.tenantId());
		ParsedCertificate parsedCertificate = certificatePemParser.parse(request.certificatePem(), request.chainPem());
		CertificateUpsertResult result = versionRepository
			.findByTenantIdAndSha256Fingerprint(request.tenantId(), parsedCertificate.sha256Fingerprint())
			.map(existingVersion -> new CertificateUpsertResult(accessibleCertificate(existingVersion.certificateId()), existingVersion, false))
			.orElseGet(() -> createCertificate(
				request.tenantId(),
				normalizedOwner(request.owner()),
				normalizeTags(request.tags()),
				parsedCertificate,
				"Certificate discovered from source observation."));

		ObservationUpsertResult observationResult = upsertSourceObservation(
			result.certificate(),
			result.version(),
			normalizeSourceType(request.sourceType()),
			normalizedRequired(request.sourceKey(), "Source key"),
			normalizedRequired(request.observedResourceKey(), "Observed resource key"),
			normalizedOptional(request.sourceName()),
			normalizeSourceMetadata(request.sourceMetadata()));

		appendAuditEvent(
			result.certificate().tenantId(),
			"certificate.source_observed",
			result.certificate().id(),
			"Certificate source observation recorded.",
			"{\"sourceType\":\"" + observationResult.observation().sourceType()
				+ "\",\"createdObservation\":" + observationResult.created()
				+ ",\"importedCertificate\":" + result.created() + "}");
		return CertificateObservationResponse.from(
			result.certificate(),
			result.version(),
			observationResult.observation(),
			result.created(),
			observationResult.created());
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
			sourceObservationRepository.findByCertificateIdOrderByLastSeenAtDesc(certificate.id()),
			metadataEntryRepository.findByCertificateIdOrderByKeyAsc(certificate.id()),
			statusHistoryRepository.findByCertificateIdOrderByChangedAtDesc(certificate.id()),
			auditEventRepository.findTop25ByResourceTypeAndResourceIdOrderByOccurredAtDesc(
				"certificate",
				certificate.id().toString()));
	}

	@Transactional
	public CertificateTagsResponse updateTags(UUID certificateId, CertificateTagsUpdateRequest request) {
		ManagedCertificate certificate = accessibleCertificate(certificateId);
		Set<String> tags = normalizeTags(request.tags());
		certificate.replaceTags(tags, clock.instant());
		appendAuditEvent(
			certificate.tenantId(),
			"certificate.tags_updated",
			certificate.id(),
			"Certificate tags updated.",
			"{\"tagCount\":" + tags.size() + "}");
		return CertificateTagsResponse.from(certificate);
	}

	@Transactional
	public CertificateMetadataResponse updateMetadata(UUID certificateId, CertificateMetadataUpdateRequest request) {
		ManagedCertificate certificate = accessibleCertificate(certificateId);
		Map<String, CertificateMetadataValueResponse> metadata = normalizeMetadata(request.metadata());
		Instant now = clock.instant();
		metadataEntryRepository.deleteByCertificateId(certificate.id());
		List<CertificateMetadataEntry> entries = metadata.entrySet().stream()
			.map(entry -> CertificateMetadataEntry.create(
				certificate.id(),
				certificate.tenantId(),
				entry.getKey(),
				entry.getValue().type(),
				entry.getValue().value(),
				CurrentActor.actorId(),
				now))
			.toList();
		metadataEntryRepository.saveAll(entries);
		certificate.touch(now);
		appendAuditEvent(
			certificate.tenantId(),
			"certificate.metadata_updated",
			certificate.id(),
			"Certificate custom metadata updated.",
			"{\"metadataKeys\":\"" + String.join(",", metadata.keySet()) + "\"}");
		return new CertificateMetadataResponse(certificate.id(), metadata);
	}

	@Transactional
	public CertificateStatusUpdateResponse updateStatus(UUID certificateId, CertificateStatusUpdateRequest request) {
		ManagedCertificate certificate = accessibleCertificate(certificateId);
		String reason = normalizedRequired(request.reason(), "Status change reason");
		CertificateStatus fromStatus = certificate.status();
		if (fromStatus == request.status()) {
			CertificateStatusHistoryResponse latest = statusHistoryRepository
				.findByCertificateIdOrderByChangedAtDesc(certificate.id())
				.stream()
				.findFirst()
				.map(CertificateStatusHistoryResponse::from)
				.orElse(null);
			return new CertificateStatusUpdateResponse(certificate.id(), certificate.status(), false, latest);
		}

		Instant now = clock.instant();
		certificate.transitionStatus(request.status(), now);
		CertificateStatusHistory history = statusHistoryRepository.save(CertificateStatusHistory.create(
			certificate.id(),
			certificate.tenantId(),
			fromStatus,
			request.status(),
			reason,
			CurrentActor.actorId(),
			now));
		appendAuditEvent(
			certificate.tenantId(),
			"certificate.status_changed",
			certificate.id(),
			reason,
			"{\"fromStatus\":\"" + fromStatus.name() + "\",\"toStatus\":\"" + request.status().name() + "\"}");
		return new CertificateStatusUpdateResponse(
			certificate.id(),
			certificate.status(),
			true,
			CertificateStatusHistoryResponse.from(history));
	}

	@Transactional(readOnly = true)
	public List<CertificateStatusHistoryResponse> statusHistory(UUID certificateId) {
		ManagedCertificate certificate = accessibleCertificate(certificateId);
		return statusHistoryRepository.findByCertificateIdOrderByChangedAtDesc(certificate.id())
			.stream()
			.map(CertificateStatusHistoryResponse::from)
			.toList();
	}

	private CertificateImportResponse duplicateImportResponse(UUID tenantId, CertificateVersion existingVersion) {
		ManagedCertificate certificate = accessibleCertificate(existingVersion.certificateId());
		appendAuditEvent(
			tenantId,
			"certificate.import_duplicate",
			certificate.id(),
			"Duplicate certificate import reused existing version.",
			"{\"sha256Fingerprint\":\"" + existingVersion.sha256Fingerprint() + "\"}");
		return CertificateImportResponse.from(certificate, existingVersion, false);
	}

	private CertificateImportResponse createImportedCertificate(CertificateImportRequest request, ParsedCertificate parsedCertificate) {
		CertificateUpsertResult result = createCertificate(
			request.tenantId(),
			normalizedOwner(request.owner()),
			normalizeTags(request.tags()),
			parsedCertificate,
			"Certificate imported.");

		appendAuditEvent(
			request.tenantId(),
			"certificate.imported",
			result.certificate().id(),
			"Certificate imported.",
			"{\"sha256Fingerprint\":\"" + result.version().sha256Fingerprint() + "\",\"status\":\"" + result.certificate().status().name() + "\"}");
		return CertificateImportResponse.from(result.certificate(), result.version(), true);
	}

	private CertificateUpsertResult createCertificate(
			UUID tenantId,
			String owner,
			Set<String> tags,
			ParsedCertificate parsedCertificate,
			String statusReason) {
		Instant now = clock.instant();
		CertificateStatus status = parsedCertificate.notAfter().isBefore(now) ? CertificateStatus.EXPIRED : CertificateStatus.ACTIVE;
		ManagedCertificate certificate = certificateRepository.save(ManagedCertificate.create(
			tenantId,
			owner,
			owner == null,
			status,
			parsedCertificate,
			tags,
			now));
		CertificateVersion version = versionRepository.save(CertificateVersion.importVersion(
			certificate.id(),
			tenantId,
			parsedCertificate,
			now));
		for (int index = 0; index < parsedCertificate.chain().size(); index++) {
			chainEntryRepository.save(CertificateChainEntry.from(version.id(), index + 1, parsedCertificate.chain().get(index)));
		}
		certificate.assignCurrentVersion(version.id(), now);
		statusHistoryRepository.save(CertificateStatusHistory.create(
			certificate.id(),
			certificate.tenantId(),
			null,
			status,
			statusReason,
			CurrentActor.actorId(),
			now));
		return new CertificateUpsertResult(certificate, version, true);
	}

	private ObservationUpsertResult upsertSourceObservation(
			ManagedCertificate certificate,
			CertificateVersion version,
			String sourceType,
			String sourceKey,
			String observedResourceKey,
			String sourceName,
			Map<String, String> sourceMetadata) {
		String packedMetadata = CertificateTextValues.packMap(sourceMetadata);
		return sourceObservationRepository
			.findByTenantIdAndSourceTypeAndSourceKeyAndObservedResourceKeyAndCertificateVersionId(
				certificate.tenantId(),
				sourceType,
				sourceKey,
				observedResourceKey,
				version.id())
			.map(existing -> {
				existing.recordSeen(sourceName, packedMetadata, clock.instant());
				return new ObservationUpsertResult(existing, false);
			})
			.orElseGet(() -> new ObservationUpsertResult(sourceObservationRepository.save(CertificateSourceObservation.create(
				certificate.id(),
				version.id(),
				certificate.tenantId(),
				sourceType,
				sourceKey,
				observedResourceKey,
				sourceName,
				version.sha256Fingerprint(),
				packedMetadata,
				clock.instant())), true));
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
					default -> {
						if (field.startsWith("metadata.")) {
							predicates.add(metadataFilter(root, query, criteriaBuilder, field, value));
						}
						else {
							throw new ApiException(ApiErrorCode.VALIDATION_FAILED, "Unsupported certificate filter field: " + filter.field());
						}
					}
				}
			}
			return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
		};
	}

	private static Predicate metadataFilter(
			Root<ManagedCertificate> root,
			jakarta.persistence.criteria.CriteriaQuery<?> query,
			jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
			String field,
			String value) {
		if (query == null) {
			throw new ApiException(ApiErrorCode.VALIDATION_FAILED, "Metadata filters are not available for this query.");
		}
		String metadataKey = normalizeMetadataKey(field.substring("metadata.".length()));
		Subquery<UUID> subquery = query.subquery(UUID.class);
		Root<CertificateMetadataEntry> metadata = subquery.from(CertificateMetadataEntry.class);
		subquery.select(metadata.get("certificateId"));
		subquery.where(
			criteriaBuilder.equal(metadata.get("certificateId"), root.get("id")),
			criteriaBuilder.equal(metadata.get("key"), metadataKey),
			criteriaBuilder.like(
				criteriaBuilder.lower(metadata.get("value")),
				"%" + value.toLowerCase(Locale.ROOT) + "%"));
		return criteriaBuilder.exists(subquery);
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

	private ManagedCertificate accessibleCertificate(UUID certificateId) {
		return certificateRepository.findById(certificateId)
			.filter(this::canAccess)
			.orElseThrow(() -> new ResourceNotFoundException("Certificate", certificateId));
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

	private static String normalizedOptional(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		String normalized = value.trim();
		if (containsLineBreak(normalized)) {
			throw new ApiException(ApiErrorCode.VALIDATION_FAILED, "Value cannot contain line breaks.");
		}
		return normalized;
	}

	private static String normalizedRequired(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new ApiException(ApiErrorCode.VALIDATION_FAILED, fieldName + " is required.");
		}
		String normalized = value.trim();
		if (containsLineBreak(normalized)) {
			throw new ApiException(ApiErrorCode.VALIDATION_FAILED, fieldName + " cannot contain line breaks.");
		}
		return normalized;
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

	private static String normalizeSourceType(String sourceType) {
		String normalized = sourceType == null ? "" : sourceType.trim().toLowerCase(Locale.ROOT);
		if (!SOURCE_TYPE_PATTERN.matcher(normalized).matches()) {
			throw new ApiException(
				ApiErrorCode.VALIDATION_FAILED,
				"Source type must be 1-64 lowercase letters, numbers, dots, underscores, colons, or dashes.");
		}
		return normalized;
	}

	private static Map<String, String> normalizeSourceMetadata(Map<String, String> metadata) {
		if (metadata == null || metadata.isEmpty()) {
			return Map.of();
		}
		Map<String, String> normalized = new TreeMap<>();
		for (var entry : metadata.entrySet()) {
			String key = normalizeMetadataKey(entry.getKey());
			String value = normalizedRequired(entry.getValue(), "Source metadata value");
			if (value.length() > 512) {
				throw new ApiException(ApiErrorCode.VALIDATION_FAILED, "Source metadata values must be at most 512 characters.");
			}
			normalized.put(key, value);
		}
		return normalized;
	}

	private static Map<String, CertificateMetadataValueResponse> normalizeMetadata(
			Map<String, CertificateMetadataValueRequest> metadata) {
		if (metadata == null || metadata.isEmpty()) {
			return Map.of();
		}
		Map<String, CertificateMetadataValueResponse> normalized = new LinkedHashMap<>();
		for (var entry : new TreeMap<>(metadata).entrySet()) {
			String key = normalizeMetadataKey(entry.getKey());
			rejectSensitiveMetadataKey(key);
			CertificateMetadataValueRequest value = entry.getValue();
			if (value == null || value.type() == null) {
				throw new ApiException(ApiErrorCode.VALIDATION_FAILED, "Metadata values must include a type.");
			}
			normalized.put(key, new CertificateMetadataValueResponse(value.type(), normalizeMetadataValue(value)));
		}
		return normalized;
	}

	private static String normalizeMetadataKey(String key) {
		String normalized = key == null ? "" : key.trim().toLowerCase(Locale.ROOT);
		if (!METADATA_KEY_PATTERN.matcher(normalized).matches()) {
			throw new ApiException(
				ApiErrorCode.VALIDATION_FAILED,
				"Metadata keys must be 1-64 lowercase letters, numbers, dots, underscores, colons, or dashes and start with a letter.");
		}
		return normalized;
	}

	private static String normalizeMetadataValue(CertificateMetadataValueRequest value) {
		String normalized = normalizedRequired(value.value(), "Metadata value");
		if (normalized.length() > 1024) {
			throw new ApiException(ApiErrorCode.VALIDATION_FAILED, "Metadata values must be at most 1024 characters.");
		}
		return switch (value.type()) {
			case STRING -> normalized;
			case NUMBER -> normalizeNumber(normalized);
			case BOOLEAN -> normalizeBoolean(normalized);
			case INSTANT -> normalizeInstant(normalized);
		};
	}

	private static String normalizeNumber(String value) {
		try {
			return new BigDecimal(value).stripTrailingZeros().toPlainString();
		}
		catch (NumberFormatException exception) {
			throw new ApiException(ApiErrorCode.VALIDATION_FAILED, "NUMBER metadata values must be valid decimals.");
		}
	}

	private static String normalizeBoolean(String value) {
		String normalized = value.toLowerCase(Locale.ROOT);
		if (!"true".equals(normalized) && !"false".equals(normalized)) {
			throw new ApiException(ApiErrorCode.VALIDATION_FAILED, "BOOLEAN metadata values must be true or false.");
		}
		return normalized;
	}

	private static String normalizeInstant(String value) {
		try {
			return Instant.parse(value).toString();
		}
		catch (DateTimeParseException exception) {
			throw new ApiException(ApiErrorCode.VALIDATION_FAILED, "INSTANT metadata values must use ISO-8601 instants.");
		}
	}

	private static void rejectSensitiveMetadataKey(String key) {
		String compact = key.replaceAll("[^a-z0-9]", "");
		if (compact.contains("password")
				|| compact.contains("secret")
				|| compact.contains("token")
				|| compact.contains("privatekey")
				|| compact.contains("apikey")
				|| compact.contains("credential")) {
			throw new ApiException(ApiErrorCode.VALIDATION_FAILED, "Metadata keys must not describe secrets or credentials.");
		}
	}

	private static boolean containsLineBreak(String value) {
		return value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0;
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

	private record CertificateUpsertResult(ManagedCertificate certificate, CertificateVersion version, boolean created) {
	}

	private record ObservationUpsertResult(CertificateSourceObservation observation, boolean created) {
	}
}
