package com.clm.platform.domain.identity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.clm.platform.security.AuthorityNames;
import com.clm.platform.security.PlatformUserPrincipal;

@Service
public class PlatformUserDetailsService implements UserDetailsService {

	private final UserAccountRepository userAccountRepository;

	private final UserRoleAssignmentRepository userRoleAssignmentRepository;

	public PlatformUserDetailsService(
		UserAccountRepository userAccountRepository,
		UserRoleAssignmentRepository userRoleAssignmentRepository) {
		this.userAccountRepository = userAccountRepository;
		this.userRoleAssignmentRepository = userRoleAssignmentRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserAccount userAccount = userAccountRepository.findByEmailIgnoreCase(username)
			.orElseThrow(() -> new UsernameNotFoundException("User was not found."));
		Set<GrantedAuthority> authorities = new HashSet<>();
		Set<UUID> tenantIds = new HashSet<>();
		boolean globalAccess = false;
		for (UserRoleAssignment assignment : userRoleAssignmentRepository.findByUserId(userAccount.id())) {
			if (assignment.tenantId() == null) {
				globalAccess = true;
			}
			else {
				tenantIds.add(assignment.tenantId());
			}
			authorities.add(new SimpleGrantedAuthority(AuthorityNames.role(assignment.role())));
			assignment.role().permissions().stream()
				.map(AuthorityNames::permission)
				.map(SimpleGrantedAuthority::new)
				.forEach(authorities::add);
		}

		return new PlatformUserPrincipal(
			userAccount.id(),
			userAccount.email(),
			userAccount.displayName(),
			userAccount.passwordHash(),
			userAccount.status() == AccountStatus.ACTIVE,
			authorities,
			tenantIds,
			globalAccess);
	}
}
