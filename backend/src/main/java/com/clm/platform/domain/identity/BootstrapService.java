package com.clm.platform.domain.identity;

import java.time.Clock;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clm.platform.api.error.ConflictException;
import com.clm.platform.domain.audit.AuditDecision;
import com.clm.platform.domain.audit.AuditEventCommand;
import com.clm.platform.domain.audit.AuditEventService;
import com.clm.platform.domain.audit.AuditStatus;
import com.clm.platform.domain.audit.AuditActorType;
import com.clm.platform.domain.tenancy.Organization;
import com.clm.platform.domain.tenancy.OrganizationRepository;
import com.clm.platform.domain.tenancy.Tenant;
import com.clm.platform.domain.tenancy.TenantRepository;
import com.clm.platform.security.BuiltInRole;

@Service
public class BootstrapService {

	private final UserAccountRepository userAccountRepository;

	private final UserRoleAssignmentRepository userRoleAssignmentRepository;

	private final TenantRepository tenantRepository;

	private final OrganizationRepository organizationRepository;

	private final PasswordEncoder passwordEncoder;

	private final AuditEventService auditEventService;

	private final Clock clock;

	public BootstrapService(
		UserAccountRepository userAccountRepository,
		UserRoleAssignmentRepository userRoleAssignmentRepository,
		TenantRepository tenantRepository,
		OrganizationRepository organizationRepository,
		PasswordEncoder passwordEncoder,
		AuditEventService auditEventService,
		Clock clock) {
		this.userAccountRepository = userAccountRepository;
		this.userRoleAssignmentRepository = userRoleAssignmentRepository;
		this.tenantRepository = tenantRepository;
		this.organizationRepository = organizationRepository;
		this.passwordEncoder = passwordEncoder;
		this.auditEventService = auditEventService;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public boolean isBootstrapRequired() {
		return userAccountRepository.count() == 0;
	}

	@Transactional
	public BootstrapAdminResponse createInitialAdmin(BootstrapAdminRequest request) {
		if (!isBootstrapRequired()) {
			throw new ConflictException("Bootstrap admin has already been created.");
		}
		if (userAccountRepository.existsByEmailIgnoreCase(request.email())) {
			throw new ConflictException("User email already exists.");
		}

		var now = clock.instant();
		Tenant tenant = tenantRepository.save(Tenant.createDefault(request.tenantSlug(), request.tenantName(), now));
		Organization organization = organizationRepository.save(Organization.create(
			tenant.id(),
			request.organizationSlug(),
			request.organizationName(),
			now));
		UserAccount userAccount = userAccountRepository.save(new UserAccount(
			UUID.randomUUID(),
			request.email().toLowerCase(),
			request.displayName(),
			passwordEncoder.encode(request.password()),
			now));
		userRoleAssignmentRepository.save(new UserRoleAssignment(
			UUID.randomUUID(),
			userAccount.id(),
			null,
			null,
			BuiltInRole.ADMIN,
			now));

		auditEventService.append(new AuditEventCommand(
			AuditActorType.SYSTEM,
			"bootstrap",
			tenant.id().toString(),
			"identity.bootstrap_admin_created",
			"user",
			userAccount.id().toString(),
			AuditDecision.ALLOW,
			AuditStatus.SUCCESS,
			"Initial administrator created.",
			null,
			null));

		return new BootstrapAdminResponse(userAccount.id(), tenant.id(), organization.id(), userAccount.email());
	}
}
