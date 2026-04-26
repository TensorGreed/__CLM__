package com.clm.platform.domain.identity;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleAssignmentRepository extends JpaRepository<UserRoleAssignment, UUID> {

	List<UserRoleAssignment> findByUserId(UUID userId);
}
