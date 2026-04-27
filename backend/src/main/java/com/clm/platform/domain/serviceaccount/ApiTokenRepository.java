package com.clm.platform.domain.serviceaccount;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiTokenRepository extends JpaRepository<ApiToken, UUID> {

	Optional<ApiToken> findByTokenHash(String tokenHash);

	List<ApiToken> findByServiceAccountIdOrderByCreatedAtDesc(UUID serviceAccountId);
}
